/* plans.js – modern Grant Match frontend */

document.addEventListener("DOMContentLoaded", () => {
    // -------------------------------
    // Config
    // -------------------------------
    const API_BASE = "http://localhost:8080/api"; // Spring Boot gateway
  
    // Rotating suggestions for the hero search placeholder
    const SUGGESTIONS = [
      "Danilo Diedrichs",
      "Amy Peeler",
      "Andrew Abernethy",
      "Jane Doe (demo)",
      "Search faculty..."
    ];
    const SUGGESTION_INTERVAL_MS = 4000;
  
    // Page state
    const state = {
      slug: null,
      page: 0,
      size: 10,
      totalPages: 0,
    };
  
    // -------------------------------
    // DOM refs
    // -------------------------------
    const form      = document.getElementById("searchForm");
    const input     = document.getElementById("profNameInput");
    const searchBtn = document.getElementById("searchBtn");
    const titleEl   = document.getElementById("prof-title");
    const resultsEl = document.getElementById("results");
    const toastEl   = document.getElementById("toast");
    const pagWrap   = document.getElementById("pagination");
    const prevBtn   = document.getElementById("prevPage");
    const nextBtn   = document.getElementById("nextPage");
    const pageInfo  = document.getElementById("pageInfo");
    const modalRoot = document.getElementById("modal-root");
  
    // -------------------------------
    // Helpers
    // -------------------------------
    const slugify = (s) =>
      s.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "");
  
    function showToast(msg) {
      toastEl.textContent = msg;
      toastEl.classList.remove("hidden");
      setTimeout(() => toastEl.classList.add("hidden"), 5000);
    }
    toastEl.addEventListener("click", () => toastEl.classList.add("hidden"));
  
    // Loading skeleton cards
    function showSkeleton(count = 3) {
      resultsEl.innerHTML = "";
      for (let i = 0; i < count; i++) {
        const div = document.createElement("div");
        div.className =
          "animate-pulse p-6 bg-white rounded-2xl shadow flex flex-col gap-3";
        div.innerHTML = `
          <div class="h-5 bg-gray-200 rounded w-3/4"></div>
          <div class="h-4 bg-gray-200 rounded w-1/2"></div>
          <div class="h-3 bg-gray-200 rounded w-full"></div>
        `;
        resultsEl.appendChild(div);
      }
    }
  
    function clearResults(msg = "") {
      resultsEl.innerHTML = "";
      if (msg) {
        const div = document.createElement("div");
        div.className = "text-center text-gray-400 py-8";
        div.textContent = msg;
        resultsEl.appendChild(div);
      }
    }
  
    // Score bar 0..1 -> %
    function scoreBar(score) {
      const pct = Math.max(0, Math.min(1, score)) * 100;
      return `
        <div class="w-full h-2 rounded bg-gray-200 overflow-hidden">
          <div class="h-full bg-blue-500" style="width:${pct}%"></div>
        </div>`;
    }
  
    function statusBadge(status) {
      const s = (status || "").toLowerCase();
      if (s === "posted")      return `<span class="px-2 py-0.5 text-xs rounded-full bg-green-100 text-green-700 font-semibold">Posted</span>`;
      if (s === "forecasted")  return `<span class="px-2 py-0.5 text-xs rounded-full bg-yellow-100 text-yellow-700 font-semibold">Forecasted</span>`;
      return `<span class="px-2 py-0.5 text-xs rounded-full bg-gray-200 text-gray-500 font-semibold">${status || "Unknown"}</span>`;
    }
  
    // -------------------------------
    // Render ranking cards
    // -------------------------------
    function renderRankings(data) {
      resultsEl.innerHTML = "";
      if (!data.content?.length) {
        clearResults("No matching grants found.");
        return;
      }
  
      data.content.forEach((row) => {
        const card = document.createElement("div");
        card.className =
          "p-6 bg-white rounded-2xl shadow hover:shadow-lg transition flex flex-col gap-4 fade-in";
  
        const scoreDisp = Number.isFinite(row.faissScore)
          ? row.faissScore.toFixed(3)
          : "—";
  
        card.innerHTML = `
          <div class="flex flex-col sm:flex-row sm:items-start gap-4">
            <div class="sm:w-20 flex sm:flex-col items-center sm:items-start gap-2">
              <span class="font-mono text-blue-700 font-bold text-lg">${scoreDisp}</span>
              ${scoreBar(row.faissScore)}
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-xl font-semibold text-gray-800 break-words">
                ${row.link
                  ? `<a href="${row.link}" target="_blank" class="text-blue-600 hover:underline">${row.title}</a>`
                  : row.title}
              </h3>
              <div class="mt-1">${statusBadge(row.status)}</div>
            </div>
            <div class="sm:self-center">
              ${
                row.hasPlan
                  ? `<button
                      class="plan-btn px-4 py-2 rounded-xl bg-blue-500 hover:bg-blue-700 text-white font-medium shadow transition"
                      data-opp="${row.oppNo}"
                      data-title="${encodeURIComponent(row.title)}"
                    >
                      View Plan
                    </button>`
                  : `<span class="text-gray-300 text-2xl cursor-not-allowed" title="No plan available yet">—</span>`
              }
            </div>
          </div>
          <div class="plan-slot mt-4 hidden"></div>
        `;
  
        if (row.hasPlan) {
          card.querySelector(".plan-btn").addEventListener("click", async (e) => {
            const btn   = e.currentTarget;
            const oppNo = btn.dataset.opp;
            const slot  = card.querySelector(".plan-slot");
            if (!slot.classList.contains("hidden")) {
              // collapse
              slot.classList.add("hidden");
              slot.innerHTML = "";
              btn.textContent = "View Plan";
              return;
            }
            btn.disabled = true;
            btn.textContent = "Loading…";
            try {
              const plansRes = await fetch(
                `${API_BASE}/prof-plans/${state.slug}/${encodeURIComponent(oppNo)}`,
                { mode: "cors" }
              );
              if (!plansRes.ok) throw new Error(await plansRes.text());
              const plans = await plansRes.json();
              slot.innerHTML = renderPlanDetails(plans);
              slot.classList.remove("hidden");
              btn.textContent = "Hide Plan";
            } catch (err) {
              showToast("Could not load plan.");
              btn.textContent = "View Plan";
            } finally {
              btn.disabled = false;
            }
          });
        }
  
        resultsEl.appendChild(card);
      });
    }
  
    function renderPlanDetails(plans) {
      if (!plans?.length) {
        return `<div class="text-sm text-gray-400">No plan details cached.</div>`;
      }
      return plans
        .map(
          (p) => `
          <details open class="group mb-3">
            <summary class="cursor-pointer select-none font-semibold text-gray-700 group-open:text-blue-700">
              ${p.rationale}
            </summary>
            <ul class="list-disc list-inside mt-1 text-gray-600 space-y-1">
              ${p.steps.map((s) => `<li>${s}</li>`).join("")}
            </ul>
          </details>`
        )
        .join("");
    }
  
    // -------------------------------
    // Pagination controls
    // -------------------------------
    function updatePagination(page, totalPages) {
      state.page = page;
      state.totalPages = totalPages;
      if (totalPages <= 1) {
        pagWrap.classList.add("hidden");
        return;
      }
      pagWrap.classList.remove("hidden");
      pageInfo.textContent = `Page ${page + 1} of ${totalPages}`;
      prevBtn.disabled = page <= 0;
      nextBtn.disabled = page >= totalPages - 1;
    }
  
    prevBtn.addEventListener("click", () => {
      if (state.page > 0) fetchRankings(state.slug, state.page - 1);
    });
    nextBtn.addEventListener("click", () => {
      if (state.page < state.totalPages - 1) fetchRankings(state.slug, state.page + 1);
    });
  
    // -------------------------------
    // Fetch rankings (main network call)
    // -------------------------------
    async function fetchRankings(slug, page = 0) {
      state.slug = slug;
      showSkeleton(); // loading skeleton
      try {
        const res = await fetch(
          `${API_BASE}/rankings/${slug}?page=${page}&size=${state.size}`,
          { mode: "cors" }
        );
        if (!res.ok) {
          throw new Error(`${res.status} ${res.statusText}`);
        }
        const data = await res.json();
        renderRankings(data);
        updatePagination(data.number, data.totalPages);
      } catch (err) {
        clearResults();
        showToast("Error loading rankings: " + err.message);
        updatePagination(0, 0);
      }
    }
  
    // -------------------------------
    // Rotating placeholder suggestions
    // -------------------------------
    let suggIdx = 0;
    function cyclePlaceholder() {
      if (document.activeElement === input) return; // don't change while typing
      if (input.value.trim() !== "") return;
      input.placeholder = SUGGESTIONS[suggIdx];
      suggIdx = (suggIdx + 1) % SUGGESTIONS.length;
    }
    cyclePlaceholder();
    setInterval(cyclePlaceholder, SUGGESTION_INTERVAL_MS);
  
    // -------------------------------
    // Form submit
    // -------------------------------
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const name = input.value.trim();
      if (!name) {
        showToast("Please enter a professor name.");
        return;
      }
      const slug = slugify(name);
      titleEl.textContent = `Grant rankings for “${name}”`;
      titleEl.classList.remove("hidden");
      fetchRankings(slug, 0);
    });
  
    // Enter manually triggers submit if user bypasses button focus
    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        form.requestSubmit();
      }
    });
  });
  