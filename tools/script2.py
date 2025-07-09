import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import OneHotEncoder
from scipy.sparse import hstack

# Load NIH data
df = pd.read_csv("nih_awards_enrichedFULL.csv", low_memory=False)

# Preprocess
df["text"] = (df["project_title"].fillna("") + " " + df["abstract_text"].fillna("")).str.lower()
df["organization_type.name"] = df["organization_type.name"].fillna("Unknown").str.lower()
df["organization.org_state"] = df["organization.org_state"].fillna("Unknown").str.upper()

# Train models
tfidf = TfidfVectorizer(stop_words="english", max_features=1000)
text_vectors = tfidf.fit_transform(df["text"])

enc = OneHotEncoder(sparse_output=False, handle_unknown="ignore")
cat_features = df[["organization_type.name", "organization.org_state"]]
encoded_cats = enc.fit_transform(cat_features)

# Save models
joblib.dump(tfidf, "tfidf_vectorizer.pkl")
joblib.dump(enc, "encoder.pkl")
print("✅ Models saved.")
