import subprocess

path = '../java'
cmd = "java -cp " + path + " Main"

times = 100
s = 0
for i in range(times):
    s += float(subprocess.check_output(cmd_paillier, shell=True).strip())

print(s/times)
