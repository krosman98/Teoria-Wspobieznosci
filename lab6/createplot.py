import csv
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from sklearn.preprocessing import LabelEncoder
import pandas as pd
plt.close('all')
if __name__ == '__main__':
    with open('results.csv') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        for row in csv_reader:
            line = []
            for val in row:
                line.append(val)


csv_reader = pd.read_csv('results.csv', sep=',', names=['0','1','2','3'])


mask = list()
for x in [2,3]:
    mask.append(csv_reader.iloc[:,x].unique().tolist())

fig, axList = plt.subplots(ncols=4, nrows=4, figsize=(16, 16))
axList2 = axList.flatten()
i=0
for x1 in mask[0]:
    for x2 in mask[1]:
        tmp = (csv_reader['2'] == x1).multiply(csv_reader['3'] == x2)
        y = csv_reader.loc[tmp]
        axList2[i].plot(y.iloc[:, 0], y.iloc[:, 1], ls='', marker='o')
        axList2[i].set(title="{}, {}".format(x1, x2))
        i=i+1
plt.tight_layout()
plt.show()
fig.savefig('plot.png')



