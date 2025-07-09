import sqlite3
import pandas as pd

# Paths
csv_file = "nih_awards_enrichedFULL.csv"
sqlite_file = "../nih_grants.sqlite"
table_name = "nih_grants"

# Load the CSV (adjust chunk size if needed for performance)
df = pd.read_csv(csv_file)

# Save to SQLite
with sqlite3.connect(sqlite_file) as conn:
    df.to_sql(table_name, conn, if_exists="replace", index=False)

print("✅ Loaded into SQLite successfully!")
