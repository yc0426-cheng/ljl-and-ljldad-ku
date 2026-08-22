# practice_1
# 计算器，实现加减乘除
first_number = float(input("请输入第一个数: "))
second_number = float(input("请输入第二个数: "))
sign = input("请输入运算符：")

match sign:
    case "加号" | '+':
        print(first_number + second_number)
    case "减号" | '-':
        print(first_number - second_number)
    case "乘号" | '*':
        print(first_number * second_number)
    case "除号" | '/' if second_number != 0: #if条件成立匹配case
        print(first_number / second_number)
    case _:
        print("操作不支持!!!")