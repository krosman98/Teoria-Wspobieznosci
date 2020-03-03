import csv
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


csv_reader = pd.read_csv('results.csv', sep=',', names=['0','1','2','3','4','5','6'])


mask = list()
for x in [0, 1, 3, 4, 5]:
    mask.append(csv_reader.iloc[:,x].unique().tolist())

fig, axList = plt.subplots(ncols=6, nrows=6, figsize=(36, 36))
axList2 = axList.flatten()
axList2[32].axis('off')
axList2[33].axis('off')
axList2[34].axis('off')
axList2[35].axis('off')
i=0
for x1 in mask[0]:
    for x2 in mask[1]:
        for x3 in mask[2]:
            for x4 in mask[3]:
                for x5 in mask[4]:

                    tmp = (csv_reader['0'] == x1).multiply(csv_reader['1'] == x2).multiply(csv_reader['3'] == x3).multiply(csv_reader['4'] == x4).multiply(csv_reader['5'] == x5)
                    y = csv_reader.loc[tmp]
                    axList2[i].plot(y.iloc[:, 2], y.iloc[:, 6], ls='', marker='o')
                    axList2[i].set(title="{}, {}, {}, {}, {}".format(x1, x2, x3, x4, x5))
                    i=i+1
plt.tight_layout()
plt.show()
fig.savefig('plot.png')



