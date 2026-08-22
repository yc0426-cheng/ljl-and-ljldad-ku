# 打印国际象棋盘
rad = 8

for i in range(rad):
    if i % 2 == 0:  # 偶数行
        for j in range(rad):
            if j % 2 == 0:
                print('黑', end=' ')
            else:
                print('白', end=' ')
    else:  # 奇数行
        for j in range(rad):
            if j % 2 == 0:
                print('白', end=' ')
            else:
                print('黑', end=' ')
    print()

print()

for i in range(rad):
    for j in range(rad):
        if (i + j) % 2 == 0:
            print('黑', end=' ')
        else:
            print('白', end=' ')
    print()
