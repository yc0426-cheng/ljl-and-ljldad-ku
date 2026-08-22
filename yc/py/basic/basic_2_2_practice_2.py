# 计算100-500之间所有三的倍数的数字之和
total = 0

start = 100

while start % 3 != 0:
    start += 1

for i in range(start, 501, 3):
    total += i
else:
    print(f'计算100-500之间所有三的倍数的数字之和: {total}')
