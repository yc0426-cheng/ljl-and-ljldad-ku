# 嵌套循环
length = int(input("请输入长方形的高： "))
width = int(input("请输入长方形的宽： "))

for i in range(length):
    for j in range(width):
        print('*', end='  ')
    print()
