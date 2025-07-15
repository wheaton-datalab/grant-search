# rank_grants_cli.py
import sys
import json
import pandas as pd
import joblib
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import hstack
from numpy.linalg import norm

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

# Compute similarity only if user vector is non-zero
if user_vector.count_nonzero() == 0:
    print("[!] Warning: User input did not match trained vocabulary or categories. Falling back to keyword ranking.")
    df["similarity_score"] = df["text"].str.contains(user["department"].lower(), na=False).astype(int)
else:
    df["similarity_score"] = cosine_similarity(user_vector, combined_vectors).flatten()

# Debug print block
print("\nDEBUG OUTPUT ------------------------------")
print("Text vector shape (grants):", text_vectors.shape)
print("Encoded category shape (grants):", encoded_cats.shape)
print("Combined feature shape:", combined_vectors.shape)
print("User text vector nonzeros:", user_text_vec.count_nonzero())
print("User categorical vector nonzeros:", user_cat_encoded.sum())
print("User vector shape:", user_vector.shape)

print("\nTop 5 similarity scores:", df["similarity_score"].head().tolist())
print("\nExample grant text (truncated):")
print(df["text"].iloc[0][:500])
print("\nUser department text:", user["department"].lower())
print("---------------------------------------------\n")



# Sort and output
ranked = df.sort_values(by="similarity_score", ascending=False)
with open(output_path, "w") as f:
    json.dump(ranked.to_dict(orient="records"), f, indent=2)

for _, grant in ranked.head(10).iterrows():
    print(f"{grant['number']} | {grant['title'][:60]}... | Score: {grant['similarity_score']:.3f}")


print("[OK] Ranking complete.")
