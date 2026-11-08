# 生成一个1-20的平方列表
# 列表推导式: 要插入的值 for i in 列表/序列
square_list = [i * i for i in range(1, 21)]

print(square_list)

# 提取所有偶数并计算平方，生成新列表
new_list = [value ** 2 for value in square_list if value % 2 == 0]

print(new_list)
