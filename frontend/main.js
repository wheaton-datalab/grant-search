// Dynamic API URL depending on environment
const API_URL =
  window.location.hostname === "localhost"
    ? "http://localhost:8080"
    : "https://grant-search.onrender.com";

console.log("Using API_URL:", API_URL);

window.onload = function () {

  // csv exporter
  let currentResults = [];

  function escapeCsvField(str) {
    if (str == null) return "";
    return `"${str.replace(/"/g, '""')}"`;
  }


  document.getElementById("export-btn").addEventListener("click", function () {
    if (currentResults.length === 0) return;

    const csvHeader = [
      "ID",
      "Number",
      "Title",
      "Agency",
      "Status",
      "Open Date",
      "Close Date",
      "CFDA",
      "Description",
    ];

    const csvRows = currentResults.map(grant => [
      `=HYPERLINK("${grant.url}", "${grant.id}")`,
      grant.number,
      grant.title,
      grant.agency,
      grant.oppStatus,
      grant.openDate || "",
      grant.closeDate || "",
      grant.cfdaList?.join(";") || "",
      grant.description
    ].map(escapeCsvField));

    const csvContent = [csvHeader, ...csvRows]
      .map(row => row.join(","))
      .join("\n");


    const blob = new Blob([csvContent], { type: "text/csv" });
    const url = URL.createObjectURL(blob);

    const downloadLink = document.createElement("a");
    downloadLink.href = url;
    const now = new Date();
    const timestamp = now.toISOString().replace(/[:.]/g, "-");
    downloadLink.download = `grants_${timestamp}.csv`;
    downloadLink.click();

    URL.revokeObjectURL(url);
  });

  // search form handling
  const form = document.querySelector("form");

  form.addEventListener("submit", async function (e) {
    e.preventDefault();

    const data = {
      keyword: document.getElementById("keyword").value,
      oppStatuses: document.getElementById("oppStatuses").value
        .split(",")
        .map(s => s.trim())
        .filter(s => s !== ""),
      agencies: document.getElementById("agencies").value
        .split(",")
        .map(s => s.trim())
        .filter(s => s !== ""),
      fundingCategories: document.getElementById("fundingCategories").value
        .split(",")
        .map(s => s.trim())
        .filter(s => s !== ""),
      rows: parseInt(document.getElementById("rows").value)
    };

    try {
      const res = await fetch(`${API_URL}/search`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      if (!res.ok) {
        throw new Error(`Server responded with status ${res.status}`);
      }

      const results = await res.json();

      currentResults = results;
      document.getElementById("export-btn").disabled = results.length === 0;

      // display results
      const resultsContainer = document.getElementById("results-container");
      resultsContainer.innerHTML = "";

      if (results.length === 0) {
        resultsContainer.innerHTML = "<p>No results found.</p>";
      } else {
       results.forEach(grant => {
          const displayDescription = grant.description === "(No synopsis)" 
            ? "Check webpage for details" 
            : grant.description || "(None)";

          const div = document.createElement("div");
          div.className = "result-card";

          div.innerHTML = `
            <h3>${grant.title}</h3>
            <p><strong>ID:</strong> ${grant.id}</p>
            <p><strong>Number:</strong> ${grant.number}</p>
            <p><strong>Agency:</strong> ${grant.agency}</p>
            <p><strong>Status:</strong> ${grant.oppStatus}</p>
            <p><strong>Open Date:</strong> ${grant.openDate || "N/A"}</p>
            <p><strong>Close Date:</strong> ${grant.closeDate || "N/A"}</p>
            <p><strong>CFDA:</strong> ${grant.cfdaList?.join(", ") || "None"}</p>
            <p><strong>Description:</strong> ${displayDescription}</p>
            <p><a href="${grant.url}" target="_blank">View on Grants.gov</a></p>
          `;

          resultsContainer.appendChild(div);
        });
      }

    } catch (error) {
      console.error("Search request failed:", error);
    }
  });
};
