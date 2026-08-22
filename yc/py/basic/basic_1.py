# math... case... 模式匹配

day = input("请输入月份: ")

match int(day):
    case 3 | 4 | 5:
        print("春天")
    case 6 | 7 | 8:
        print("夏天")
    case 9 | 10 | 11:
        print("秋天")
    case 12 | 1 | 2:
        print("冬天")
    case _:
        print("请输入1-12内的值")