#!/usr/bin/env python3
import time
import json
import sqlite3
import pandas as pd
from collections import Counter
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import requests
from bs4 import BeautifulSoup

# Constants
DB_PATH = "wheaton_faculty.db"
FACULTY_URL = "https://www.wheaton.edu/academics/faculty/"

def collect_profile_links(scroll_pause=1.2, max_idle=3):
    opts = Options()
    opts.add_argument("--headless=new")
    serv = Service(ChromeDriverManager().install())
    drv = webdriver.Chrome(service=serv, options=opts)
    drv.get(FACULTY_URL)
    idle, prev = 0, set()
    while idle < max_idle:
        drv.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(scroll_pause)
        anchors = drv.find_elements(By.CSS_SELECTOR, "a[href*='/academics/faculty/']")
        curr = {a.get_attribute("href") for a in anchors 
                if a.get_attribute("href").count("/") > 5}
        if curr == prev:
            idle += 1
        else:
            prev, idle = curr, 0
    drv.quit()
    return sorted(prev)

def parse_faculty_sections(url):
    r = requests.get(url, timeout=20)
    soup = BeautifulSoup(r.text, "html.parser")
    out = {"url": url}
    out["name"] = soup.find("h1").get_text(strip=True) if soup.find("h1") else None
    for sec in soup.select("section[id^='faculty-']"):
        h2 = sec.find("h2")
        if not h2:
            continue
        key = h2.get_text(strip=True)
        sib = h2.find_next_sibling()
        if not sib:
            continue
        if sib.name == "p":
            val = sib.get_text(" ", strip=True)
        elif sib.name == "ul":
            val = [li.get_text(strip=True) for li in sib.find_all("li")]
        else:
            val = sib.get_text(" ", strip=True)
        out[key] = val
    return out

def clean_df(df):
    # normalize column names
    cols = [c.strip().lower().replace(" ", "_") for c in df.columns]
    cnt = Counter()
    new_cols = []
    for c in cols:
        cnt[c] += 1
        new_cols.append(c if cnt[c] == 1 else f"{c}_{cnt[c]-1}")
    df.columns = new_cols

    # convert list columns to JSON strings
    for c in df.columns:
        if df[c].apply(lambda x: isinstance(x, list)).any():
            df[c] = df[c].apply(lambda x: json.dumps(x) if isinstance(x, list) else x)
    return df

def main():
    print("Collecting faculty profile links...")
    links = collect_profile_links()
    print(f"Found {len(links)} profiles.")

    records = []
    for i, url in enumerate(links, 1):
        try:
            print(f"[{i}/{len(links)}] Parsing {url}")
            records.append(parse_faculty_sections(url))
        except Exception as e:
            print(f"  ❗ Error parsing {url}: {e}")

    print("Building DataFrame...")
    df = pd.DataFrame(records)

    print("Cleaning data...")
    df = clean_df(df)

    print(f"Saving to SQLite database at {DB_PATH}...")
    conn = sqlite3.connect(DB_PATH)
    df.to_sql("faculty", conn, if_exists="replace", index=False)
    conn.close()

    print("Faculty profiles saved.")

if __name__ == "__main__":
    main()
