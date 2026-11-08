# 根据提供的学生成绩单，完成如下需求：
#
# 1. 计算每个学生的总分、各科平均分，然后一并输出出来。
# 2. 统计各科成绩的最低分、最高分、平均分，并输出。
# 3. 查找成绩优秀（平均分大于90）的学生，并输出。

students = (
    (1, "张三", 85, 92, 88),
    (2, "李四", 95, 98, 92),
    (3, "王五", 78, 85, 80),
    (4, "赵六", 92, 88, 96)
)

for student in students:
    sum = 0
    for i in student[2::1]:
        sum += i
    print(sum)
    print(sum / 3)

chinese_scores = [s[2] for s in students]
math_scores = [s[3] for s in students]
english_scores = [s[4] for s in students]
print(max(chinese_scores))
print(min(chinese_scores))
print(max(math_scores))
print(min(math_scores))
print(max(english_scores))
print(min(english_scores))

avg_scores = [(s[2] + s[3] + s[4]) / 3 for s in students]
print(max(avg_scores))
