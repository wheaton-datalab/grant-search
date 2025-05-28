window.onload = function () {
  const form = document.querySelector("form");

  form.addEventListener("submit", async function (e) {
    e.preventDefault(); // prevent page reload

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

    //console.log("Sending search request with:", data);

    try {
      const res = await fetch("http://localhost:8080/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      if (!res.ok) {
        throw new Error(`Server responded with status ${res.status}`);
      }

      const results = await res.json();
      //console.log("Received search results:", results);

    } catch (error) {
      console.error("Search request failed:", error);
    }
  });
};
