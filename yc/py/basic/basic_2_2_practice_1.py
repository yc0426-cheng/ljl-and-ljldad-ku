# 计算1-100之间的奇数之和
total = 0

for i in range(1, 101, 2):
    total += i
else:
    print(f'计算1-100之间的奇数之和: {total}')
