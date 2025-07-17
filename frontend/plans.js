/** plans.js **/
const { useState, useEffect } = React;

// ==================================
// CONFIG
// ==================================
const API_BASE = "http://localhost:8080/api";
const RESULTS_PER_PAGE = 10;
const PLACEHOLDER_SUGGESTIONS = [
  "Search by faculty name...",
  "e.g., Danilo Diedrichs",
  "e.g., Amy Peeler",
  "e.g., Andrew Abernethy",
];

// ==================================
// COMPONENTS
// ==================================
const SkeletonCard = () => (
  <div className="bg-white p-6 rounded-2xl border border-slate-200 animate-pulse mb-4"></div>
);

const EmptyState = ({ query }) => (
  <div className="text-center py-16 px-6 bg-white rounded-2xl border border-slate-200">
    <h3 className="text-xl font-semibold text-slate-800">No Matches Found</h3>
    <p className="text-slate-500 mt-2">We couldn't find any grants for "{query}".</p>
  </div>
);

const PlanDetails = ({ plans, isLoading, error }) => {
  if (isLoading) {
    return <div className="mt-4 pt-5 border-t border-slate-200 animate-pulse h-24"></div>;
  }
  if (error) {
    return <div className="mt-4 pt-5 border-t border-slate-200 text-sm text-red-600">{error}</div>;
  }
  if (!plans?.length) {
    return <div className="mt-4 pt-5 border-t border-slate-200 text-sm text-slate-500">No plan details available.</div>;
  }
  return (
    <div className="mt-4 pt-5 border-t border-slate-200 space-y-5">
      {plans.map((plan, idx) => (
        <div key={idx}>
          <h4 className="font-semibold text-slate-800">{plan.rationale}</h4>
          <ul className="list-disc list-inside text-slate-600 ml-4 mt-1 space-y-1 text-sm">
            {plan.steps.map((s,i)=><li key={i}>{s}</li>)}
          </ul>
        </div>
      ))}
    </div>
  );
};

const GrantCard = ({ grant, profSlug }) => {
  const [visible, setVisible] = useState(false);
  const [state, setState]     = useState({ data:null, isLoading:false, error:null });
  const toggle = async () => {
    if (visible) return setVisible(false);
    setVisible(true);
    if (!state.data) {
      setState({data:null,isLoading:true,error:null});
      try {
        const res = await fetch(`${API_BASE}/prof-plans/${profSlug}/${encodeURIComponent(grant.oppNo)}`);
        if (!res.ok) throw new Error("Could not load plan details.");
        const data = await res.json();
        setState({data, isLoading:false, error:null});
      } catch(e) {
        setState({data:null,isLoading:false,error:e.message});
      }
    }
  };
  const statusObj = {
    posted:    "bg-green-100 text-green-800",
    forecasted:"bg-yellow-100 text-yellow-800"
  }[grant.status?.toLowerCase()] || "bg-slate-100 text-slate-800";

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-200 hover:shadow-md transition mb-4 animate-fadeIn">
      <h3 className="text-lg font-bold text-slate-800 hover:text-blue-600">
        <a href={grant.link} target="_blank" rel="noopener noreferrer">{grant.title}</a>
      </h3>
      <p className="text-sm text-slate-500 mt-1">Score: {grant.faissScore.toFixed(3)}</p>
      <div className="mt-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <span className={`px-3 py-1 text-xs font-semibold rounded-full ${statusObj}`}>
          {grant.status}
        </span>
        {grant.hasPlan && (
          <button
            onClick={toggle}
            className="px-5 py-2 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 transition"
          >
            {visible ? "Hide Plan" : "View Application Plan"}
          </button>
        )}
      </div>
      {visible && <PlanDetails {...state} />}
    </div>
  );
};

// ==================================
// MAIN APP
// ==================================
function App() {
  const [query, setQuery]           = useState("");
  const [placeholder, setPlaceholder] = useState(PLACEHOLDER_SUGGESTIONS[0]);
  const [isLoading, setIsLoading]   = useState(false);
  const [results, setResults]       = useState([]);
  const [error, setError]           = useState(null);
  const [profSlug, setProfSlug]     = useState(null);
  const [searched, setSearched]     = useState(null);

  // subscription UI
  const [email, setEmail]           = useState("");
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [subLoading, setSubLoading] = useState(false);

  useEffect(() => { // placeholder cycling
    const i = setInterval(()=>{
      setPlaceholder(p => {
        const idx = PLACEHOLDER_SUGGESTIONS.indexOf(p);
        return PLACEHOLDER_SUGGESTIONS[(idx+1)%PLACEHOLDER_SUGGESTIONS.length];
      });
    }, 4000);
    return ()=>clearInterval(i);
  },[]);

  useEffect(() => { // load sub status
    if (!profSlug || !/\S+@\S+\.\S+/.test(email)) return;
    setSubLoading(true);
    fetch(`${API_BASE}/subscriptions?slug=${profSlug}`)
      .then(r=>r.json())
      .then(list=>setIsSubscribed(list.includes(email)))
      .catch(()=>setIsSubscribed(false))
      .finally(()=>setSubLoading(false));
  },[profSlug,email]);

  const handleSearch = async e => {
    e.preventDefault();
    if (!query.trim()) return;
    setIsLoading(true);
    setError(null);
    setResults([]);
    setSearched(query);
    const slug = query.toLowerCase().replace(/[^a-z0-9]+/g,"-").replace(/^-+|-+$/g,"");
    setProfSlug(slug);
    try {
      const res = await fetch(`${API_BASE}/rankings/${slug}?page=0&size=${RESULTS_PER_PAGE}`);
      if (!res.ok) throw new Error(`No professor named "${query}"`);
      const data = await res.json();
      setResults(data.content);
    } catch(e) {
      setError(e.message);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleSub = async () => {
    if (!/\S+@\S+\.\S+/.test(email)) return;
    setSubLoading(true);
    try {
      await fetch(`${API_BASE}/subscriptions`, {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({ email, slug:profSlug, enabled:!isSubscribed })
      });
      setIsSubscribed(!isSubscribed);
    } catch {
      alert("Failed to update subscription");
    } finally {
      setSubLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-16">
      <header className="text-center mb-12">
        <h1 className="text-5xl font-extrabold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-500">
          Grant Match Finder
        </h1>
        <p className="mt-2 text-slate-500">Search a professor to see active grant matches.</p>
      </header>

      <form onSubmit={handleSearch} className="flex gap-3 max-w-2xl mx-auto mb-8">
        <input
          value={query}
          onChange={e=>setQuery(e.target.value)}
          placeholder={placeholder}
          className="flex-1 px-6 py-4 rounded-2xl border border-slate-300 focus:ring-2 focus:ring-blue-300 outline-none"
        />
        <button
          type="submit"
          disabled={isLoading}
          className="px-8 py-4 rounded-2xl bg-blue-600 text-white font-semibold disabled:opacity-50"
        >
          {isLoading ? "Searching..." : "Search"}
        </button>
      </form>

      {profSlug && !isLoading && !error && (
        <div className="max-w-2xl mx-auto mb-8 bg-white p-4 rounded-2xl border border-slate-200 flex gap-3">
          <input
            type="email"
            value={email}
            onChange={e=>setEmail(e.target.value)}
            placeholder="you@domain.edu"
            className="flex-1 px-4 py-2 rounded-full border border-slate-300"
          />
          <button
            onClick={toggleSub}
            disabled={subLoading || !/\S+@\S+\.\S+/.test(email)}
            className={`px-6 py-2 rounded-full text-white ${
              isSubscribed ? "bg-red-500 hover:bg-red-600" : "bg-green-600 hover:bg-green-700"
            } disabled:opacity-50`}
          >
            {subLoading ? "…" : isSubscribed ? "Unsubscribe" : "Subscribe"}
          </button>
        </div>
      )}

      <main className="space-y-4">
        {isLoading && Array.from({ length: 5 }).map((_,i)=><SkeletonCard key={i}/>)}
        {!isLoading && error && <div className="text-center py-16 bg-red-50 text-red-700 rounded-2xl">{error}</div>}
        {!isLoading && !error && searched && results.length===0 && <EmptyState query={searched}/>}
        {!isLoading && !error && results.map(g=><GrantCard key={g.oppNo} grant={g} profSlug={profSlug}/>)}
      </main>
    </div>
  );
}

// MOUNT
const root = ReactDOM.createRoot(document.getElementById("grant-finder-app"));
root.render(<App />);
