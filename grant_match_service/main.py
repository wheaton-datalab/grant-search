from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import List

from pipeline import build_profile, semantic_search, score_grants, generate_plans
from pipeline import GrantPlan, Plan

app = FastAPI()
app.add_middleware(
  CORSMiddleware,
  allow_origins=["http://localhost:3000"],
  allow_methods=["GET","POST","OPTIONS"],
  allow_headers=["*"],
)

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
