# predict_awards_cli.py

import sys
import json
import pandas as pd
import joblib
import numpy as np
from scipy.sparse import hstack

# Load trained model and encoders
model = joblib.load("tools/award_predictor.pkl")
encoder = joblib.load("tools/encoder.pkl")
tfidf = joblib.load("tools/tfidf_vectorizer.pkl")

# Read input/output paths
input_path = sys.argv[1]
output_path = sys.argv[2]

# Load input JSON file
with open(input_path, "r") as f:
    data = json.load(f)

user = data["user"]
results = data["results"]

# Convert to DataFrame
df = pd.DataFrame(results)

# Text features: lowercase title + description
df["text"] = (df["title"].fillna("") + " " + df["description"].fillna("")).str.lower()
text_vecs = tfidf.transform(df["text"])

# Categorical features from user
X_input = pd.DataFrame({
    "organization_type.name": [user["institutionType"].lower()] * len(df),
    "organization.org_state": [user["state"].upper()] * len(df)
})
encoded_cats = encoder.transform(X_input)

# Combine features like we did in training
X_combined = hstack([text_vecs, encoded_cats])

# Predict and attach
predicted_awards = model.predict(X_combined)
df["predictedAward"] = np.round(predicted_awards, 2)

# Save output
with open(output_path, "w") as f:
    json.dump(df.to_dict(orient="records"), f, indent=2)

print("[OK] Award prediction complete.")
