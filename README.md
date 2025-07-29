# grant-search

A full-stack application to crawl, search, rank, and enrich grant opportunities from Grants.gov, providing tailored results for users (such as professors) based on their institution, department, and location. Machine learning models are used to match users with appropriate grants.

## Important links

GitHub: https://github.com/wheaton-datalab/grant-search
search2 API: https://grants.gov/api/common/search2
fetchOppurtunity API: https://www.grants.gov/api/common/fetchopportunity
Data to train model (Request if unavailable): https://drive.google.com/file/d/1dNmCEpkL-2fz7BypzC6544QONAx2-0Xm/view?usp=sharing

## Usage

1. Enter your search criteria (keywords, statuses, agencies, etc.).
2. Optionally provide your department, institution type, and state for personalized ranking.
3. Submit the form to view ranked and enriched grant opportunities.
4. Export results as CSV if desired.

## Architecture

- **Frontend:** JavaScript (Vanilla), HTML, CSS, hosted on Netlify
- **Backend:** Java (Spring Boot), REST API, hosted on Render
- **Python Scripts:** For ranking and award prediction, invoked by the backend

```
[User] <--> [Frontend (Netlify)] <--> [Backend API (Render)] <--> [Grants.gov API]
                                                      |
                                                      +--> [Python ML Scripts]
```

---

## Getting Started

### Prerequisites

- Java 17+
- Python 3.8+
- Maven (for backend)
- Grants.gov API access (public endpoints used)

## Deployment

- **Frontend:** Deployed to Netlify (auto-deploy from `frontend/`)
- **Backend:** Deployed to Render (auto-deploy from main branch)
- **Python Scripts:** Must be present in backend working directory on Render

## Notes and problems from the author

Hello, my name is Gavin and I made this program. AI was used in much of the structure and documentation. My time was up before I could fix/finish what I had started, and unfortunately the current version is not functional. Below is a list of items that whomever works on this next will need to work on first. The Render and Netlify hosts are under personal accounts, so my successor will need to create their own account or find an alternative host program. An unitended effect of that is that you will understand the program a bit more if you have no experiance with host sites. This is how I run it in the terminal for testing:

mvn clean install
java -jar target/grants-harvester-1.0-SNAPSHOT.jar

It's not the most efficient code base, but it's enough of a foundation to where you can do something pretty impressive pretty quick (after you fix the bugs). This document has most of the info to get you started, but if you have any questions, contact me at gavin.mcclowry@my.wheaton.edu. Best of wishes to you as you work on this project.

Needs to be done:
- Set up Render and Netlify to host (or alternative hosting platforms)
- Fix 500 status bug (found on the inspect/network page, shouldn't be too complicated, will help you understand how the project works)
- Update matching model (will likely need a full evaluation)