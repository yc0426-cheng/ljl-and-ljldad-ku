# 列表
# list = [元素,元素]
# 可以存储不同类型的值
# 元素有序，可重复，可修改

s = [1, 2, 4, True, 'cwyg']

print(type(s))

s[3] = False

print(s[3])

del s

# 切片

#   序列数据[开始索引:结束索引:步长]
# •不包含结束索引位置对应的元素（开始索引未指定默认为0;结束索引未指定默认为列表长度（直到列表末尾）;步长未指定默认为1）
# •索引采用正向、反向索引都可以
# •步长是选取间隔，默认步长为1

string = ["A", "B", "C", "D", "E", "F", "G"]

s_front_1 = string[1:len(string):2]
s_front_2 = string[1:-1:2]
print(s_front_1 == s_front_2)  # ['B', 'D', 'F']

s_end_1 = string[-2:-len(string):-2]  # ['F', 'D', 'B']
s_end_2 = string[-2::-2]  # ['F', 'D', 'B']
print(s_end_1 == s_end_2)

number_list = [1, 111, 432, 65, 59, 78, 76, 256]

number_list.append(122121)

number_list.insert(1, 78)

number_list.remove(1)

number_list.pop()  # 不写默认是最后一个

number_list.sort()

number_list.reverse()

print(number_list)
