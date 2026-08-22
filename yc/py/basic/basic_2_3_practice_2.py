# 根据输入的三角形变成 打印等腰直角三角形
radis = int(input("请输入三角形边长："))

for i in range(1, radis + 1):
    for j in range(1, i + 1):
        print('*', end='  ')
    print()
