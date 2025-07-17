from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Optional

from pydantic import BaseModel
from pipeline import build_profile, semantic_search, score_grants, generate_plans
from pipeline import GrantPlan, Plan

app = FastAPI()
app.add_middleware(
  CORSMiddleware,
  allow_origins=["http://localhost:3000"],
  allow_methods=["GET","POST","OPTIONS"],
  allow_headers=["*"],
)

class RankHit(BaseModel):
    oppNo: str
    title: str
    status: str
    link: Optional[str] = ""     
    faissScore: float  

@app.get("/rank", response_model=List[RankHit])
async def rank(slug: str, k: int = 50):
    try:
        profile = build_profile(slug)
    except ValueError:
        raise HTTPException(404, "Professor not found")

    hits = semantic_search(profile, k=k, include_scores=True)  
    # Filter active (posted/forecasted)
    active = [h for h in hits
              if isinstance(h.get("oppStatus"), str)
              and h["oppStatus"].lower() in ("posted", "forecasted")]

    out = []
    for h in active:
        raw_link = h.get("link")
        # Coerce link to a real str, otherwise ""
        link = raw_link if isinstance(raw_link, str) else ""
        out.append(RankHit(
            oppNo      = str(h.get("oppNum", "")),
            title      = str(h.get("title", "")),
            status     = str(h.get("oppStatus", "")),
            link       = link,
            faissScore = float(h.get("score", 0.0))
        ))
    return out

@app.get("/match", response_model=List[GrantPlan])
async def match(slug: str):
    try:
        profile = build_profile(slug)
    except ValueError:
        raise HTTPException(404, "Professor not found")

    # 1) search
    cands   = semantic_search(profile, k=25)
    # 2) score
    scored  = score_grants(profile, cands)
    # 3) sort + take top 5
    top5    = sorted(scored, key=lambda g: g["fitScore"], reverse=True)[:5]
    # 4) generate plans for top 3
    plans   = [generate_plans(profile, g) for g in top5[:3]]

    # 5) assemble output
    out = []
    for g, pl in zip(top5[:3], plans):
        out.append(GrantPlan(
            title=g["title"],
            fitScore=g["fitScore"],
            link=g["link"],
            opportunityStatus=g["oppStatus"],
            plans=pl
        ))
    return out
