from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional

class Plan(BaseModel):
    rationale: str
    steps: List[str]

class GrantPlan(BaseModel):
    title: str
    fitScore: float
    link: str
    opportunityStatus: Optional[str] = None
    plans: List[Plan]

app = FastAPI()

@app.get("/match", response_model=List[GrantPlan])
def match(slug: str):
    if slug == "test-prof":
        return [
            GrantPlan(
                title="Dynamics, Control and Systems Diagnostics",
                fitScore=9.1,
                link="https://example.com/dcsd",
                opportunityStatus="Posted",
                plans=[
                    Plan(
                        rationale="Leverage nonlinear dynamics expertise to improve diagnostics.",
                        steps=[
                            "Highlight bifurcation theory background.",
                            "Integrate prior models of epidemics.",
                            "Create joint project with control-systems team."
                        ]
                    ),
                    Plan(
                        rationale="Use supply-chain modelling work for real-world control tests.",
                        steps=[
                            "Showcase UAV cargo simulation.",
                            "Prototype decision-support dashboard.",
                            "Partner with humanitarian org for validation."
                        ]
                    )
                ]
            )
        ]
    else:
        raise HTTPException(status_code=404, detail="Professor not found")
