// Dynamic API URL depending on environment (localhost for dev, production URL otherwise)
const API_URL =
  window.location.hostname === "localhost"
    ? "http://localhost:8080"
    : "https://grant-search.onrender.com";


console.log("Using API_URL:", API_URL);

window.onload = function () {

  // Store the current search results for CSV export
  let currentResults = [];

  /**
   * Escapes a string for safe inclusion in a CSV field.
   * Doubles quotes and wraps the field in quotes.
   * @param {string} str
   * @returns {string}
   */
  function escapeCsvField(str) {
    if (str == null) return "";
    return `"${str.replace(/"/g, '""')}"`;
  }

  // Handle CSV export button click
  document.getElementById("export-btn").addEventListener("click", function () {
    if (currentResults.length === 0) return;

    // Define CSV header row
    const csvHeader = [
      "ID",
      "Name",
      "Number",
      "Agency",
      "Open Date",
      "Close Date",
      "Description",
      "Award Floor",
      "Award Ceiling",
    ];

    // Map each grant result to a CSV row
    const csvRows = currentResults.map(grant => {
      let rawDesc = grant.description || "";
      if (rawDesc === "(No synopsis)") {
        rawDesc = "Check webpage for full details";
      }

      // Strip HTML tags from description
      const cleanedDesc = rawDesc.replace(/<[^>]*>/g, "").trim();

      return [
          `=HYPERLINK("${grant.url}", "${grant.id}")`,
          grant.title,
          grant.number,
          grant.agency,
          grant.openDate || "",
          grant.closeDate || "",
          cleanedDesc,
          grant.awardFloor || "",
          grant.awardCeiling || "",
      ].map(escapeCsvField);
    });

    // Combine header and rows, join as CSV string
    const csvContent = [csvHeader, ...csvRows]
      .map(row => row.join(","))
      .join("\n");

    // Create a Blob and trigger download
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

  // Handle search form submission
  const form = document.querySelector("form");

  form.addEventListener("submit", async function (e) {
    e.preventDefault();

    // Gather form data and build request payload :)
    const data = {
      keyword: document.getElementById("keyword").value,
      oppStatuses: document.getElementById("oppStatuses").value
        .split(",")
        .map(s => s.trim())
        .filter(s => s !== ""),
      fundingInstruments: document.getElementById("fundingInstruments").value
        .split(",")
        .map(s => s.trim())
        .filter(s => s !== ""),
      rows: parseInt(document.getElementById("rows").value),

      department: document.getElementById("department")?.value.trim(),
      institutionType: document.getElementById("institutionType")?.value.trim(),
      userState: document.getElementById("state")?.value
    };

    try {
      // Send POST request to backend /search endpoint
      const res = await fetch(`${API_URL}/search`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      if (!res.ok) {
        throw new Error(`Server responded with status ${res.status}`);
      }

      // Parse JSON response
      const results = await res.json();

      // Store results for export
      currentResults = results;
      document.getElementById("export-btn").disabled = results.length === 0;

      // Display results in the results container
      const resultsContainer = document.getElementById("results-container");
      resultsContainer.innerHTML = "";

      if (results.length === 0) {
        resultsContainer.innerHTML = "<p>No results found.</p>";
      } else {
        results.forEach(grant => {
         const displayDescription = grant.description === "(No synopsis)" 
            ? "Check webpage for details" 
            : grant.description?.replace(/<[^>]*>/g, "").trim() || "(None)";

          const div = document.createElement("div");
          div.className = "result-card";

          div.innerHTML = `
            <h3>${grant.title}</h3>
            <p><strong>ID:</strong> <a href="${grant.url}" target="_blank">${grant.id}</a></p>
            <p><strong>Number:</strong> ${grant.number}</p>
            <p><strong>Agency:</strong> ${grant.agency}</p>
            <p><strong>Open Date:</strong> ${grant.openDate || "N/A"}</p>
            <p><strong>Close Date:</strong> ${grant.closeDate || "N/A"}</p>
            <p><strong>Description:</strong> ${displayDescription}</p>
            <p><strong>Award Floor:</strong> ${grant.awardFloor || "N/A"}</p>
            <p><strong>Award Ceiling:</strong> ${grant.awardCeiling || "N/A"}</p>
          `;

          resultsContainer.appendChild(div);
        });
      }

    } catch (error) {
      console.error("Search request failed:", error);
    }
  });
};