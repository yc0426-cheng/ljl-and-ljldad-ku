# 根据输入数字打印对应金字塔
n = 5

for i in range(1, n + 1):
    # 打印空格
    print("  " * (n - i), end="")
    # 打印递增部分：1 到 i
    for j in range(1, i + 1):
        print(j, end=" ")
    # 打印递减部分：i-1 到 1
    for j in range(i - 1, 0, -1):
        print(j, end=" ")
    print()  # 换行