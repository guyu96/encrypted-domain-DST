from urllib.error import HTTPError
from urllib.request import Request, urlopen
from bs4 import BeautifulSoup
from random import random

def same_string(s1, s2):
    ignore = [" ", "\t", "\n", "\r"]
    ss1 = ""
    ss2 = ""
    for c in s1:
        if c not in ignore:
            ss1 += c
    for c in s2:
        if c not in ignore:
            ss2 += c
    return ss1 == ss2

def strip(s):
    ignore = [" ", "\t", "\n", "\r"]
    ss = ""
    for c in s:
        if c not in ignore:
            ss += c
    return ss

def get_minor(snp_id):
    id = snp_id[2:]
    url = "http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=" + id
    print(url)
    count = 10
    for i in range(count):
        try:
            page = urlopen(url)
        except HTTPError as e:
            print(e)
    soup = BeautifulSoup(page, "html.parser")
    allele_info = soup.find("span", {"title": "reported on genome orientation"})
    if allele_info:
        return allele_info.string[0]
    else:
        print("no allele info found")
        return None

def get_disease_snp(disease_url, disease_tag):
    req = Request(disease_url, headers={'User-Agent': 'Mozilla/5.0'})
    page = urlopen(req).read()
    soup = BeautifulSoup(page, "html.parser")
    snp_table = soup.find("table", {"id": "dna"})
    if not snp_table:
        print("table not found")
        return None
    entries = snp_table.findAll("tr")
    l = []
    found = False
    for i in range(2, len(entries)):
        entry = entries[i]
        tag = entry.find("td", {"class": "description"}).find("b")
        if found and tag:
            break
        if tag and same_string(tag.string, disease_tag):
            found = True
        if found:
            l.append(strip(entry.find("td", {"class": "snp"}).string))
    return l


def same_allele(a1, a2):
    same1 = ["G", "C"]
    same2 = ["A", "T"]
    if a1 in same1 and a2 in same1:
        return True
    if a1 in same2 and a2 in same2:
        return True
    return False

def get_state(rsid):
    minor = get_minor(rsid)
    state = 0
    print("Checking SNP with id " + rsid)
    print("Minor Allele: " + minor)
    print("Relevant Data Entries:")
    with open("dbsnp.csv") as snp:
        for line in snp:
            found = False
            i = line.find(rsid)
            if i != -1:
                next = i + len(rsid)
                if next >= len(line):
                    found = True
                elif ord(line[next]) > ord('9') or ord(line[next]) < ord('0'):
                    found = True
                if found:
                    print(line, end="")
                    data = line.split(',')
                    if same_allele(data[8], minor):
                        state += 1
    print("SNP state: " + str(state))
    return state


disease_url = "http://www.eupedia.com/genetics/autoimmune_diseases_snp.shtml"
disease_tag = "Type-2 Diabetes"
snps = get_disease_snp(disease_url, disease_tag)
ctrb = []
prob = [[], [], []]
for i in range(len(snps)):
    ctrb.append(int(random() * 10000) / 10000)
    for j in range(3):
        prob[j].append(int(random() * 10000) / 10000)

print(str(prob).replace('[', '{').replace(']', '}'))

with open("info.txt", "w") as info:
    info.write(str(snps).replace('[', '{').replace(']', '}'))
    info.write('\n')

    info.write(str(ctrb).replace('[', '{').replace(']', '}'))
    info.write('\n')

    info.write(str(prob).replace('[', '{').replace(']', '}'))
    info.write('\n')

    states = []
    for snp in snps:
        states.append(get_state(snp))
    info.write(str(states).replace('[', '{').replace(']', '}'))
    info.write('\n')

    c_sum = sum(ctrb)
    cp_sum = 0
    for i in range(len(snps)):
        cp_sum += (ctrb[i] * prob[states[i]][i])
    info.write(str(cp_sum / c_sum))
