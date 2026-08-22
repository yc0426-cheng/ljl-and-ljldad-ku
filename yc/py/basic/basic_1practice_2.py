# 简单指令系统

index = 0

while index < 6:
    str = input("请输入指令：")

    match str:
        case '上' | 'W' | 'w':
            print("角色向上移动")
        case '下' | 'S' | 's':
            print("角色向下移动")
        case '左' | 'A' | 'a':
            print("角色向左移动")
        case '右' | 'D' | 'd':
            print("角色向右移动")
        case '跳' | ' ':
            print("角色跳跃")
        case '攻击' | 'J' | 'j':
            print("角色发起攻击")
        case '退出' | 'ESC' | 'esc':
            print("角色退出游戏")

    index += 1

    print(f'已输出{index}项指令，总共5项指令')