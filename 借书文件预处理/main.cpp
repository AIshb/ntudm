#include <iostream>
#include <string>
#include <fstream>
#include <map>

using namespace std;

// 参数str表示需取子串的原字符串，num1和num2表示取值的上下界
// 返回值string表示子串
// 函数是用来取子串的
string sstr(string str, int num1, int num2)
{
    string ostr = "";

    // 每次将单个字符加到string类型的ostr的末端
    for (int i = num1; i != num2; ++i)
    {
        ostr += str[i];
    }
    return ostr;
}

int main()
{
    ifstream i1("借书_改.txt");    // 读取文本文件借书
    ifstream i2("图书类别.txt");   // 去读文本文件图书类别
    ofstream o1("借书_替换.txt");  // 存储替换后的文本文件借书
    map<string, string> m;  // 存储图书类别，键为图书号，值为图书分类号
    string istr;            // 存储从文本文件中读取的一行字符

    // 先读取一行，把第一行中文舍去
    // 然后每次在map中插入新的一对值
    getline(i2, istr);
    while (getline(i2, istr))
    {
        string a, b;
        m.insert(pair<string, string>(istr.substr(0, 7), istr.substr(8)));
    }

    // 具体思路同上一个块
    getline(i1, istr);
    o1 << istr << endl;
    while (getline(i1, istr))
    {
        int num1 = istr.find_first_of('\t', 3);         // 找到并记录第一个出现制表符'\t'的位置
        int num2 = istr.find_first_of('\t', num1 + 1);  // 找到并记录第二个出现制表符'\t'的位置
        string ostr = istr.substr(0, num1 + 1) + m[sstr(istr, num1 + 1, num2)] + istr.substr(num2);
        o1 << ostr << endl;
    }

    //关闭文件流
    i1.close();
    i2.close();
    o1.close();
    return 0;
}
