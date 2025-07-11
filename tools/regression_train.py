import pandas as pd
import joblib
from sklearn.linear_model import Ridge
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import OneHotEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
from scipy.sparse import hstack
from math import sqrt

# === STEP 1: Load the full dataset efficiently ===

dtype_map = {
    "project_num": "string",
    "project_title": "string",
    "abstract_text": "string",
    "phr_text": "string",
    "fiscal_year": "int32",
    "award_amount": "float32",
    "direct_cost_amt": "float32",
    "indirect_cost_amt": "float32",
    "funding_mechanism": "category",
    "activity_code": "category",
    "agency_ic_admin.name": "category",
    "organization.org_name": "category",
    "organization.org_state": "category",
    "organization.org_city": "category",
    "organization_type.name": "category",
    "spending_categories_desc": "category",
    "investigators": "string"
}

print("📥 Loading full CSV (this might take a minute)...")
df = pd.read_csv("nih_awards_enrichedFULL.csv", dtype=dtype_map, low_memory=False)

# Drop rows with missing target
df = df.dropna(subset=["award_amount"])

# === STEP 2: Feature preparation ===

# Combine title + abstract as a textual signal
df["text"] = (df["project_title"].fillna("") + " " + df["abstract_text"].fillna("")).str.lower()

# Vectorize text
tfidf = TfidfVectorizer(max_features=1000, stop_words="english")
X_text = tfidf.fit_transform(df["text"])

# One-hot encode key demographics
cat_cols = ["organization_type.name", "organization.org_state"]
enc = OneHotEncoder(sparse_output=True, handle_unknown="ignore")
X_cat = enc.fit_transform(df[cat_cols])

# Combine all features
X_all = hstack([X_text, X_cat])
y = df["award_amount"]

# === STEP 3: Train/test split and model training ===

X_train, X_test, y_train, y_test = train_test_split(X_all, y, test_size=0.2, random_state=42)

model = Ridge(alpha=1.0)
model.fit(X_train, y_train)

y_pred = model.predict(X_test)
rmse = sqrt(mean_squared_error(y_test, y_pred))
print(f"RMSE on test set: ${rmse:,.2f}")

# === STEP 4: Save the model and encoders ===

joblib.dump(model, "award_predictor.pkl")
joblib.dump(tfidf, "tfidf_vectorizer.pkl")   # This will be reused by the similarity scorer too
joblib.dump(enc, "encoder.pkl")

print("Saved model and vectorizers to 'tools/' directory.")
