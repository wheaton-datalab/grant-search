// plans.js

const { useState, useEffect } = React;

// ==================================
// CONFIGURATION
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
// HELPER & UI COMPONENTS
// ==================================

/** A single skeleton card for a clean loading state. No shimmer, just a clean pulse. */
const SkeletonCard = () => (
    <div className="bg-white p-6 rounded-2xl border border-slate-200 animate-pulse">
        <div className="h-4 bg-slate-200 rounded w-4/5 mb-3"></div>
        <div className="h-3 bg-slate-200 rounded w-1/3 mb-5"></div>
        <div className="flex justify-between items-center">
            <div className="flex gap-4">
                <div className="h-6 w-20 bg-slate-200 rounded-full"></div>
                <div className="h-6 w-24 bg-slate-200 rounded-full"></div>
            </div>
            <div className="h-10 w-28 bg-slate-200 rounded-lg"></div>
        </div>
    </div>
);

/** A well-designed message for when no results are found. */
const EmptyState = ({ query }) => (
    <div className="text-center py-16 px-6 bg-white rounded-2xl border border-slate-200">
        <h3 className="text-xl font-semibold text-slate-800">No Matches Found</h3>
        <p className="text-slate-500 mt-2">
            We couldn't find any grants for "{query}".
        </p>
    </div>
);


// ==================================
// CORE FEATURE COMPONENTS
// ==================================

/** Displays the collapsable plan details with clear formatting. */
const PlanDetails = ({ plans, isLoading, error }) => {
    if (isLoading) {
        return <div className="mt-4 pt-5 border-t border-slate-200/80"><div className="h-20 bg-slate-100 rounded-lg animate-pulse"></div></div>
    }
    if (error) {
        return <div className="mt-4 pt-5 border-t border-slate-200/80 text-sm text-red-600">{error}</div>
    }
    if (!plans?.length) {
        return <div className="mt-4 pt-5 border-t border-slate-200/80 text-sm text-slate-500">No plan details are available for this grant.</div>
    }

    return (
        <div className="mt-4 pt-5 border-t border-slate-200/80 space-y-5">
            {plans.map((plan, index) => (
                <div key={index}>
                    <h4 className="font-semibold text-slate-800">{plan.rationale}</h4>
                    <ul className="mt-2 ml-4 list-disc text-slate-600 space-y-1.5 text-sm">
                        {plan.steps.map((step, i) => <li key={i}>{step}</li>)}
                    </ul>
                </div>
            ))}
        </div>
    );
};

/** The main card component, redesigned for clarity and professionalism. */
const GrantCard = ({ grant, profSlug }) => {
    const [isPlanVisible, setIsPlanVisible] = useState(false);
    const [planState, setPlanState] = useState({ data: null, isLoading: false, error: null });

    const handleTogglePlan = async () => {
        if (isPlanVisible) {
            setIsPlanVisible(false);
            return;
        }

        setIsPlanVisible(true);
        if (!planState.data) { // Fetch only once
            setPlanState({ data: null, isLoading: true, error: null });
            try {
                const res = await fetch(`${API_BASE}/prof-plans/${profSlug}/${encodeURIComponent(grant.oppNo)}`);
                if (!res.ok) throw new Error("Could not load plan details.");
                const data = await res.json();
                setPlanState({ data, isLoading: false, error: null });
            } catch (err) {
                setPlanState({ data: null, isLoading: false, error: err.message });
            }
        }
    };

    const statusMap = {
        posted: { text: "Posted", class: "bg-green-100 text-green-800 ring-green-600/20" },
        forecasted: { text: "Forecasted", class: "bg-yellow-100 text-yellow-800 ring-yellow-600/20" },
    };
    const statusInfo = statusMap[grant.status?.toLowerCase()] || { text: grant.status || 'Unknown', class: 'bg-slate-100 text-slate-800 ring-slate-600/20' };

    return (
        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 transition-all duration-300 hover:shadow-md hover:border-slate-300 animate-fadeIn">
            <h3 className="text-lg font-bold text-slate-800">
                <a href={grant.link} target="_blank" rel="noopener noreferrer" className="hover:text-blue-600">
                    {grant.title}
                </a>
            </h3>
            <p className="text-sm text-slate-500 mt-1">Match Score: {grant.faissScore.toFixed(3)}</p>
            
            <div className="mt-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <span className={`px-3 py-1 text-xs font-semibold rounded-full ring-1 ring-inset ${statusInfo.class}`}>
                    {statusInfo.text}
                </span>

                {grant.hasPlan && (
                    <button onClick={handleTogglePlan} className="w-full sm:w-auto px-5 py-2 text-sm font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800 shadow-sm transition-colors">
                        {isPlanVisible ? 'Hide Plan' : 'View Application Plan'}
                    </button>
                )}
            </div>

            {isPlanVisible && <PlanDetails {...planState} />}
        </div>
    );
};


// ==================================
// MAIN APPLICATION COMPONENT
// ==================================
function App() {
    const [isLoading, setIsLoading] = useState(false);
    const [results, setResults] = useState([]);
    const [error, setError] = useState(null);
    const [searchedProf, setSearchedProf] = useState(null);
    const [profSlug, setProfSlug] = useState(null);
    
    // State for the controlled input
    const [query, setQuery] = useState("");
    const [placeholder, setPlaceholder] = useState(PLACEHOLDER_SUGGESTIONS[0]);

    // Placeholder cycling effect
    useEffect(() => {
        const interval = setInterval(() => {
            setPlaceholder(p => {
                const currentIndex = PLACEHOLDER_SUGGESTIONS.indexOf(p);
                const nextIndex = (currentIndex + 1) % PLACEHOLDER_SUGGESTIONS.length;
                return PLACEHOLDER_SUGGESTIONS[nextIndex];
            });
        }, 4000);
        return () => clearInterval(interval);
    }, []);

    const handleSearch = async (e) => {
        e.preventDefault();
        const profName = query.trim();
        if (!profName) return;

        setIsLoading(true);
        setResults([]);
        setError(null);
        setSearchedProf(profName);

        const slug = profName.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "");
        setProfSlug(slug);

        try {
            const url = `${API_BASE}/rankings/${slug}?page=0&size=${RESULTS_PER_PAGE}`;
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`We couldn't find a professor named "${profName}". Please check the spelling.`);
            }
            const data = await response.json();
            setResults(data.content);
        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto px-4 py-16 sm:py-24">
            <header className="text-center mb-12">
                <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight bg-gradient-to-r from-blue-600 via-indigo-500 to-purple-500 text-transparent bg-clip-text">
                    Grant Match Finder
                </h1>
                <p className="mt-3 text-lg text-slate-500">Search a professor to see active grant matches.</p>
            </header>

            <form onSubmit={handleSearch} className="relative flex items-center gap-2 max-w-2xl mx-auto mb-12">
                <input
                    type="text"
                    value={query}
                    onChange={e => setQuery(e.target.value)}
                    placeholder={placeholder}
                    className="flex-1 text-xl px-6 py-4 rounded-2xl border border-slate-300 shadow-sm focus:ring-4 focus:ring-blue-300 focus:border-blue-500 outline-none bg-white transition"
                />
                <button
                    type="submit"
                    disabled={isLoading}
                    className="px-8 py-4 rounded-2xl text-lg font-semibold text-white shadow-lg bg-blue-600 hover:bg-blue-700 active:scale-95 disabled:bg-slate-400 disabled:cursor-wait transition-all"
                >
                    {isLoading ? 'Searching...' : 'Search'}
                </button>
            </form>

            <main className="space-y-4">
                {isLoading && Array.from({ length: 5 }).map((_, i) => <SkeletonCard key={i} />)}
                
                {!isLoading && error && <div className="text-center py-16 px-6 bg-red-50 text-red-700 rounded-2xl border border-red-200">{error}</div>}
                
                {!isLoading && !error && searchedProf && results.length === 0 && <EmptyState query={searchedProf} />}
                
                {!isLoading && !error && results.length > 0 && results.map(grant => (
                    <GrantCard key={grant.oppNo} grant={grant} profSlug={profSlug} />
                ))}
            </main>
        </div>
    );
}

// ==================================
// MOUNT THE REACT APPLICATION
// ==================================
const container = document.getElementById('grant-finder-app');
const root = ReactDOM.createRoot(container);
root.render(<App />);