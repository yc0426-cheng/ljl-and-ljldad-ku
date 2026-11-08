# 输入一个字符串，判断该字符串是否是回文（两边对称）
# 黄山落叶松叶落山黄
# 上海自来水来自海上
# 将用户输入的10个字符串，反转后全部转换为大写，然后记录在列表中，最后将列表内容，遍历输出出来

# reverse = input('请输入一个字符串')
#
# print(reverse == reverse[::-1])

index = 0

strList = []

while index < 10:
    str = input(f"请输入第{index + 1}个字符串")
    reverseStr = str[::-1].upper()

    strList.append(reverseStr)
    index += 1

for value in strList:
    print(value)
