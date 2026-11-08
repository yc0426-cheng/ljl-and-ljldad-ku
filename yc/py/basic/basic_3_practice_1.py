# 将用户输入的10个数字，存储到一个列表中，
# 并将列表中的数字进行排序，
# 输出其中的最小值、最大值和平均值。

number_list = []

Sum = 0

for i in range(0, 10):
    number = int(input(f"请输入第{i + 1}个数字："))
    number_list.append(number)

number_list.sort()

print(number_list[0])
print(number_list[-1])

for num in number_list:
    Sum += num

print(Sum / 10)
