# 合并数组并去重

num_list = []

num_list1 = [19, 23, 54, 64, 875, 20, 109, 232, 123, 54]
num_list2 = [55, 80, 72, 35, 60, 123, 54, 29, 91]
#
# for num in num_list2:
#     num_list1.append(num)

# 解包
num_list = [*num_list1, *num_list2]

num_list1.sort()

for num in num_list2:
    if num not in num_list:
        num_list.append(num)

num_list.sort()

print(num_list)
