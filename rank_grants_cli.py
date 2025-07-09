# rank_grants_cli.py
import sys
import json
import pandas as pd
import joblib
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import hstack

# Load models trained on NIH data
tfidf = joblib.load("tools/tfidf_vectorizer.pkl")
enc = joblib.load("tools/encoder.pkl")

# Read input path from CLI args
input_path = sys.argv[1]
output_path = sys.argv[2]

# Load the input JSON file
with open(input_path, "r") as f:
    data = json.load(f)

user = data["user"]  # dict with department, institutionType, state
results = data["results"]  # list of dicts with title + description

# Convert to DataFrame
df = pd.DataFrame(results)
df["text"] = (df["title"].fillna("") + " " + df["description"].fillna("")).str.lower()

# Prepare category input (same for every row)
df["institution_type"] = user["institutionType"].lower()
df["user_state"] = user["state"].upper()

# Vectorize features
text_vectors = tfidf.transform(df["text"])
repeated_cats = pd.DataFrame({
    "organization_type.name": [user["institutionType"].lower()] * len(df),
    "organization.org_state": [user["state"].upper()] * len(df)
})
encoded_cats = enc.transform(repeated_cats)
combined_vectors = hstack([text_vectors, encoded_cats])

# User vector
user_text_vec = tfidf.transform([user["department"].lower()])
user_cat_df = pd.DataFrame([[user["institutionType"].lower(), user["state"].upper()]],
                           columns=["organization_type.name", "organization.org_state"])
user_cat_encoded = enc.transform(user_cat_df)
user_vector = hstack([user_text_vec, user_cat_encoded])

# Similarity
df["similarity_score"] = cosine_similarity(user_vector, combined_vectors).flatten()

# Sort and output
ranked = df.sort_values(by="similarity_score", ascending=False)
with open(output_path, "w") as f:
    json.dump(ranked.to_dict(orient="records"), f, indent=2)

print("✅ Ranking complete.")
