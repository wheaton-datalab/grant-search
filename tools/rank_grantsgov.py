import joblib
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import hstack
import pandas as pd

# Load models
tfidf = joblib.load("tfidf_vectorizer.pkl")
enc = joblib.load("encoder.pkl")

def rank_grantsgov_results(user_input, grantsgov_results):
    df = pd.DataFrame(grantsgov_results)
    df["text"] = (df["title"].fillna("") + " " + df["description"].fillna("")).str.lower()

    # Prepare categorical context
    df["institution_type"] = user_input["institutionType"].lower()
    df["user_state"] = user_input["state"].upper()

    text_vectors = tfidf.transform(df["text"])
    repeated_cats = pd.DataFrame({
        "organization_type.name": [user_input["institutionType"].lower()] * len(df),
        "organization.org_state": [user_input["state"].upper()] * len(df)
    })
    encoded_cats = enc.transform(repeated_cats)

    combined_vectors = hstack([text_vectors, encoded_cats])

    # Build user vector
    user_text_vec = tfidf.transform([user_input["department"].lower()])
    user_cat_df = pd.DataFrame([[user_input["institutionType"].lower(), user_input["state"].upper()]],
                               columns=["organization_type.name", "organization.org_state"])
    user_cat_encoded = enc.transform(user_cat_df)
    user_vector = hstack([user_text_vec, user_cat_encoded])

    # Rank
    similarities = cosine_similarity(user_vector, combined_vectors).flatten()
    df["similarity_score"] = similarities
    return df.sort_values(by="similarity_score", ascending=False)
