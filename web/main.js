window.onload = function () {
  const form = document.querySelector("form");

  form.addEventListener("submit", async function (e) {
    e.preventDefault(); // prevent page reload

    const data = {
      keyword: document.getElementById("keyword").value,
      oppStatuses: document.getElementById("oppStatuses").value,
      agencies: document.getElementById("agencies").value.split(","),
      fundingCategories: document.getElementById("fundingCategories").value.split(","),
      rows: parseInt(document.getElementById("rows").value)
    };

    console.log("Sending search request with:", data);

    const res = await fetch("http://localhost:8080/search", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });

    const results = await res.json();
    console.log("Grants found:", results);

    // Optional: render to a table or div
  });
};
