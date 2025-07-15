import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import OneHotEncoder
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import hstack

# Load your full NIH data from SQLite or CSV
# You can use sqlite3 here if needed, but we'll assume you loaded into a DataFrame:
# Example (if using CSV):
df = pd.read_csv("nih_awards_enrichedFULL.csv", dtype={"organization.org_zipcode": str})

# For now, assume df is already a DataFrame (you can pass it into a function later)
def prepare_model(df):
    # Combine project_title + abstract_text as a 'department-like' signal
    df["text"] = (df["project_title"].fillna("") + " " + df["abstract_text"].fillna("")).str.lower()

    # Clean categorical columns
    df["organization_type.name"] = df["organization_type.name"].fillna("Unknown").str.lower()
    df["organization.org_state"] = df["organization.org_state"].fillna("Unknown").str.upper()

    # Vectorize text
    tfidf = TfidfVectorizer(stop_words="english", max_features=1000)
    text_vectors = tfidf.fit_transform(df["text"])

    # One-hot encode state + institution type
    enc = OneHotEncoder(sparse_output=False, handle_unknown="ignore")
    cat_features = df[["organization_type.name", "organization.org_state"]]
    encoded_cats = enc.fit_transform(cat_features)

    # Combine into one matrix
    combined_features = hstack([text_vectors, encoded_cats])

    return df, tfidf, enc, combined_features

# Function to score/rank grants for a new professor input
def score_grants(user_department, user_institution_type, user_state, df, tfidf, enc, combined_features):
    # Vectorize user input
    user_text_vec = tfidf.transform([user_department.lower()])

    user_cat_df = pd.DataFrame([[user_institution_type.lower(), user_state.upper()]],
        columns=["organization_type.name", "organization.org_state"])
    user_cat_encoded = enc.transform(user_cat_df)

    user_vector = hstack([user_text_vec, user_cat_encoded])
    similarities = cosine_similarity(user_vector, combined_features).flatten()

    df_with_scores = df.copy()
    df_with_scores["similarity_score"] = similarities

    return df_with_scores.sort_values(by="similarity_score", ascending=False)

# Example usage:
if __name__ == "__main__":
    df, tfidf, enc, combined_features = prepare_model(df)

    results = score_grants(
        user_department="Biochemistry",
        user_institution_type="Schools of Medicine",
        user_state="AL",
        df=df,
        tfidf=tfidf,
        enc=enc,
        combined_features=combined_features
    )

    print(results[["project_num", "project_title", "organization.org_name", "similarity_score"]].head(10))
