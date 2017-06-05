with open("dbsnp.csv", "w") as snp:
    with open("var-GS000037824-ASM.tsv") as var:
        for line in var:
            values = line.split("\t")
            if len(values) < 16:
                continue
            if values[11] == "PASS" and values[6] == "snp" and "dbsnp" in values[13]:
                snp.write(','.join(values))
