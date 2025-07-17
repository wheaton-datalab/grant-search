document.addEventListener("DOMContentLoaded", () => {
    const input      = document.getElementById("profNameInput");
    const button     = document.getElementById("searchBtn");
    const titleElem  = document.getElementById("prof-title");
    const container  = document.getElementById("container");
  
    button.addEventListener("click", async () => {
      const name = input.value.trim();
      if (!name) {
        container.textContent = "🚨 Please enter a professor name.";
        return;
      }
  
      titleElem.textContent    = `Grant plans for “${name}”`;
      container.textContent    = "⏳ Loading…";
  
      try {
        // hit our new search-by-name endpoint
        const res = await fetch(
          `http://localhost:8080/api/plans/search?name=${encodeURIComponent(name)}`,
          { mode: "cors" }
        );
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
  
        const plans = await res.json();
        container.innerHTML = "";  // clear “Loading…” text
  
        plans.forEach(g => {
          const card = document.createElement("div");
          card.className = "plan-card";
          card.innerHTML = `
            <h3>
              <a href="${g.link}" target="_blank">${g.title}</a>
              <span class="badge">${g.opportunityStatus ?? ""}</span>
            </h3>
            ${g.plans.map(p => `
              <details>
                <summary>${p.rationale}</summary>
                <ul>${p.steps.map(s => `<li>${s}</li>`).join("")}</ul>
              </details>
            `).join("")}
          `;
          container.appendChild(card);
        });
  
      } catch (err) {
        container.textContent = "❌ Error loading plans: " + err.message;
        console.error(err);
      }
    });
  });
  