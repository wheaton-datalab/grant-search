const API_URL =
  window.location.hostname === "localhost"
    ? "http://localhost:8080"
    : "https://grant-search.onrender.com";

console.log("Using API_URL:", API_URL);

window.onload = function () {

  // csv exporter
  let currentResults = [];

  // CSV escape function
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
      "URL"
    ];

    const csvRows = currentResults.map(grant => [
      grant.id,
      grant.number,
      escapeCsvField(grant.title),
      grant.agency,
      grant.oppStatus,
      grant.openDate || "",
      grant.closeDate || "",
      grant.cfdaList?.join(";") || "",
      escapeCsvField(grant.description),
      grant.url
    ]);

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

  // (your form submit stays the same — no change needed!)
};
