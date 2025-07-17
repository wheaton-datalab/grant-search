import os, json, pickle, sqlite3
import numpy as np, faiss
from datetime import date
import openai
from pydantic import BaseModel
from typing import List
import sqlite3

# Load env & API key
from dotenv import load_dotenv
load_dotenv()
openai.api_key = os.getenv("OPENAI_API_KEY")

# Paths (ensure these exist in this folder)
FACULTY_DB = "wheaton_faculty.db"
INDEX_PATH = "grant_index.faiss"
META_PATH  = "grant_metadata.pkl"

# ─── Models for output ───────────────────────────────────────────────
class Plan(BaseModel):
    rationale: str
    steps: List[str]

class GrantPlan(BaseModel):
    title: str
    fitScore: float
    link: str
    opportunityStatus: str
    plans: List[Plan]

# ─── 1) Build enriched profile ────────────────────────────────────────
def build_profile(slug: str) -> str:
    # ensure we get dict-like rows
    conn = sqlite3.connect(FACULTY_DB)
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()

    # match "/slug/" anywhere in the URL column
    stmt = "SELECT * FROM faculty WHERE url LIKE ?"
    like_pattern = f"%/{slug}/%"
    cur.execute(stmt, (like_pattern,))

    row = cur.fetchone()
    conn.close()

    if not row:
        raise ValueError(f"No faculty found for slug={slug!r}")

    # build a simple profile string from all non-null columns except url/author_ids
    parts = []
    for key in row.keys():
        if key in ("url", "author_ids"):
            continue
        val = row[key]
        if val:
            parts.append(f"{key}: {val}")

    return "\n".join(parts)


# ─── 2) Semantic search ───────────────────────────────────────────────
def semantic_search(profile: str, k=25, include_scores=False):
    idx = faiss.read_index(INDEX_PATH)
    with open(META_PATH, "rb") as f:
        recs = pickle.load(f)
    resp = openai.embeddings.create(input=[profile], model="text-embedding-3-large")
    qv = np.array([resp.data[0].embedding], dtype="float32")
    faiss.normalize_L2(qv)
    D, I = idx.search(qv, k)
    out = []
    for rank, i in enumerate(I[0]):
        r = recs[i]
        item = {
            "title": r["OPPORTUNITY TITLE"],
            "description": r["FUNDING DESCRIPTION"],
            "link":        r["LINK TO ADDITIONAL INFORMATION"],
            "oppNum":      r["OPPORTUNITY NUMBER"],
            "oppStatus":   r["OPPORTUNITY STATUS"]
        }
        if include_scores:
            item["score"] = float(D[0][rank])  # cosine similarity
        out.append(item)
    return out

# ─── 3) Score opportunities ───────────────────────────────────────────
def score_grants(profile: str, grants: List[dict]) -> List[dict]:
    scored = []
    for g in grants:
        system = "You are a pragmatic grant-match advisor. Return only JSON."
        prompt = f"Profile:\n{profile}\n\nGrant title: {g['title']}\nSynopsis: {g['description'][:300]}…"
        resp = openai.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role":"system","content":system},
                      {"role":"user",  "content":prompt}],
            functions=[{
                "name":"score_grant",
                "description":"Score a grant",
                "parameters":{
                    "type":"object",
                    "properties":{
                      "score":  {"type":"number"},
                      "reason": {"type":"string"}
                    },
                    "required":["score","reason"]
                }
            }],
            function_call={"name":"score_grant"},
            temperature=0.2
        )
        args = json.loads(resp.choices[0].message.function_call.arguments)
        g["fitScore"]  = args["score"]
        g["reason"]    = args["reason"]
        scored.append(g)
    return scored

# ─── 4) Plan generation ───────────────────────────────────────────────
def generate_plans(profile: str, grant: dict) -> List[Plan]:
    resp = openai.chat.completions.create(
        model="gpt-4.1-mini",
        messages=[{"role":"system",
                   "content":"You are a grant-writing consultant. Return only JSON."},
                  {"role":"user",
                   "content":f"Profile:\n{profile}\n\nGrant: {grant['title']}\nSynopsis: {grant['description'][:200]}…"}],
        functions=[{
          "name":"create_plans",
          "description":"Generate exactly three plans",
          "parameters":{
            "type":"object","properties":{
              "plans":{
                "type":"array","minItems":3,"maxItems":3,
                "items":{"type":"object",
                  "properties":{
                    "rationale":{"type":"string"},
                    "steps":{"type":"array","items":{"type":"string"}}
                  },
                  "required":["rationale","steps"]
                }
              }
            },
            "required":["plans"]
          }
        }],
        function_call={"name":"create_plans"},
        temperature=0.3,
        max_tokens=500
    )
    return [Plan(**p) for p in json.loads(resp.choices[0].message.function_call.arguments)["plans"]]
