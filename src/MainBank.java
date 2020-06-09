import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainBank {
    public static void main(String[] args) {
        double mainbegin = System.currentTimeMillis(); // 程序开始时间，调用系统的当前时间

        MainBank main = new MainBank();

        //输入路径
        String INDATA_PATH = "./data/test_data.txt";
//      String INDATA_PATH = "data/test_data.txt";
        //输出路径
        String OUTDATA_PATH = "./projects/student/result.txt";
//      String OUTDATA_PATH = "projects/student/result.txt";

        //node集合
        List<String> list;
        Set<Integer> set = new HashSet<>();
        Map<Integer,Integer> hp = new HashMap<>();
        int[] nodes;
        //有向图的邻接矩阵
        int[][] line;
        list = main.readFile();

        //添加节点,添加线
        int flag,start,end;
        for (String str:
                list) {
            flag = str.indexOf(",");
            start = Integer.parseInt(str.substring(0,flag));
            end = Integer.parseInt(str.substring(flag + 1));
            set.add(start);
            set.add(end);
        }
        nodes = main.addNode(set);
        main.sort(nodes,0,nodes.length - 1);
        line = new int[nodes.length][nodes.length];
        main.setMap(nodes,hp);
        main.addLine(hp,line,list);
        int[] original = nodes;
        nodes = main.deleteZeroNode(nodes,line);
        Set<String> judgeset = new HashSet<>();
        List<String> reslut = new ArrayList<>();
        main.findCircle(line,nodes,reslut,hp,original,judgeset);
        main.writeFile(reslut);

        double mainend = System.currentTimeMillis(); // 程序结束时间，调用系统当前时间
        double time = mainend - mainbegin;// 程序的运行时间
        System.out.println("运行时间为："+time  + "毫秒");
    }
    //添加节点
    private int[] addNode(Set<Integer> set){
        int[] nodes = new int[set.size()];
        int flag = 0;
        Iterator<Integer> it = set.iterator();
        while(it.hasNext()) {
            nodes[flag] = it.next();
            flag++;
        }
        return nodes;
    }
    //hashmap
    private void setMap(int[] nodes,Map<Integer,Integer> hp){
        for (int i = 0; i < nodes.length; i++) {
            hp.put(nodes[i],i);
        }
    }
    //添加线并初始化邻接数组
    private void addLine(Map<Integer,Integer> hp,int[][] line,List<String> list) {
        int flag,start,end;
        for (String str: list) {
            flag = str.indexOf(",");
            start = Integer.parseInt(str.substring(0,flag));
            end = Integer.parseInt(str.substring(flag + 1));
            start = hp.get(start);
            end = hp.get(end);
            line[start][end] = 1;
        }
    }
    //拓扑排序删去一入度为零的点
    private int[] deleteZeroNode(int[] nodes,int[][] line){
        boolean flag;
        int lengthn = 0;
        for (int i = 0; i < nodes.length; i++) {
            flag = false;
            for (int j = 0; j <= nodes.length - 1; j++) {
                if(line[j][i] == 1) {
                    flag = true;
                    lengthn++;
                    break;
                }
            }
            if (!flag){
                nodes[i] = -1;
            }
        }
        int[] node = new int[lengthn];
        int j = 0;
        for (int i: nodes) {
            if (i != -1) {
                node[j] = i;
                j++;
            }
        }
        return node;
    }
    //寻找闭环
    private void  findCircle(int[][] line,int[] nodes,List<String> reslut,Map<Integer,Integer> hp,int[] original,Set<String> judgeset) {
        // 从出发节点到当前节点的轨迹
        List<Integer> trace = new ArrayList<>();

        ExecutorService service= Executors.newFixedThreadPool(6);
        List<Future<String>> futureList=new ArrayList<Future<String>>();


        List<List<String>> alllist = new ArrayList<>();
        List<String> listthree = new ArrayList<>();
        List<String> listfour = new ArrayList<>();
        List<String> listfive = new ArrayList<>();
        List<String> listsix = new ArrayList<>();
        List<String> listseven = new ArrayList<>();

        alllist.add(listthree);
        alllist.add(listfour);
        alllist.add(listfive);
        alllist.add(listsix);
        alllist.add(listseven);
        //返回值
        if (line.length > 0) {
            for (int i: nodes) {
                int returnnum = hp.get(i);
                findCycle(hp.get(i),trace,line,returnnum,original,judgeset,alllist);

                Main.CallableTask task=new Main.CallableTask(i);
                futureList.add(service.submit(task));//按顺序放
            }
            service.shutdown();
        }
        List<String> answer = new ArrayList<>();
        for (List<String> str: alllist) {
            firstSameSettle(str,answer);
            reslut.addAll(answer);
            answer.removeAll(answer);
        }


    }
    private class CallableTask implements Callable<String> {
        Integer temp;
        public CallableTask(Integer d){
            temp=d;
        }
        @Override
        public String call() throws Exception {
            System.out.println("执行第"+temp+"个任务");
            return "第"+temp+"个任务："+Thread.currentThread().getName();
        }
    }
    //dfs
    private void findCycle(int v,List<Integer> trace,int[][] line,int returnnum,int[] original,Set<String> judgeset,List<List<String>> alllist)
    {
        //添加闭环信息
        if(trace.size() > 0 && v == returnnum) {
            int j = 0;
            StringBuilder sb = new StringBuilder();
            String stra;
            while(j<trace.size()) {
                stra = original[trace.get(j)]+",";
                if(sb.toString().contains(stra)){
                    return;
                }
                stra = original[trace.get(j)]+",";
                sb.append(stra);
                j++;
            }
            String str = sb.toString();
            str = sortString(str);
            if (!judgeset.contains(str) && (j>2 && j<8)){
                alllist.get(j - 3).add(str);
            }
            judgeset.add(str);
            return;
        }
        trace.add(v);
        if(trace.size() > 7) {
            trace.remove(trace.size()-1);
            return;
        }
        for(int i=0;i<original.length;i++){
            if(line[v][i]==1) {
                findCycle(i,trace,line,returnnum,original,judgeset,alllist);
            }
        }
        trace.remove(trace.size()-1);
    }
    //一行排序
    private String sortString(String strc){
        String[] str =  strc.split(",");
        int[] nums = new int[str.length];
        for (int i = 0; i < str.length; i++) {
            nums[i] = Integer.parseInt(str[i]);
        }
        sort(nums,0,nums.length-1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nums.length - 1; i++) {
            sb.append(nums[i]);
            sb.append(",");
        }
        sb.append(nums[nums.length - 1]);
        strc = sb.toString();
        return strc;
    }
    //快速排序
    private void sort(int[] arr,int left,int right) {
        if(left >= right) return;
        int base = arr[left];
        int lw = left, rw=right;
        while(lw!=rw) {
            //注意：此处必须先从右边找。因为基准数选的是左边第一个。
            while (lw < rw && arr[rw] >= base) rw--;
            while (lw < rw && arr[lw] <= base) lw++;
            if (lw != rw) {
                int tmp = arr[lw];
                arr[lw] = arr[rw];
                arr[rw] = tmp;
            }
        }
        if(lw!=left) {
            arr[left] = arr[lw];
            arr[lw] = base;
        }
        sort(arr,left,lw-1);
        sort(arr,lw+1,right);
    }
    //首位相等的结果排序
    private void firstSameSettle(List<String> reslut,List<String> answer){
        //取数据
        String[] str = new String[reslut.size()];
        String[] strfinal = new String[reslut.size()];
        int[] nums = new int[str.length];
        int flag;
        for (int i = 0; i < str.length; i++) {
            str[i] = reslut.get(i);
            flag = str[i].indexOf(',');
            nums[i] = Integer.parseInt(str[i].substring(0,flag));
        }
        //排序
        for (int i = 0; i < strfinal.length; i++) {
            int flagA = 2147483647,A = 0;
            boolean bigorsmall;
            for(int j = 0;j < nums.length;j++){
                if(nums[j] < flagA) {
                    flagA = nums[j];
                    A = j;
                }
                if(nums[j] == flagA){
                    bigorsmall = judgewhichisbig(reslut.get(j),reslut.get(A));
                    if(bigorsmall){
                        flagA = nums[j];
                        A = j;
                    }
                }
            }
            strfinal[i] = str[A];
            nums[A] = 2147483647;
        }
        //存数据
        for (String demo:
                strfinal) {
            answer.add(demo);
        }
    }
    //初步排序后首项相等的ID进行后续比较
    private boolean judgewhichisbig(String A,String B){
        int last = A.lastIndexOf(',') + 1;
        int endA = A.indexOf(',');
        int startA = endA + 2;
        endA = A.indexOf(',',startA);
        int endB = B.indexOf(',');
        int startB = endB + 1;
        endB = B.indexOf(',',startB);
        int J = Integer.parseInt(A.substring(startA,endA));
        int I = Integer.parseInt(B.substring(startB,endB));
        while(startA < last){
            if(J < I) return true;
            else if(J > I) return false;
            startA = endA + 1;
            endA = A.indexOf(',',startA);
            startB = endB + 1;
            endB = B.indexOf(',',startB);
            J = Integer.parseInt(A.substring(startA,endA));
            I = Integer.parseInt(B.substring(startB,endB));
        }
        return false;
    }
    //读文件
    private List<String> readFile() {
        String pathname = INDATA_PATH;
        List<String> list = new ArrayList<>();
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        //不关闭文件会导致资源的泄露，读写文件都同理
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            int i;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                i = line.lastIndexOf(",");
                line = line.substring(0,i);
                list.add(line);
                //System.out.print(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    //写文件
    private void writeFile(List<String> list) {
        try {
            File writeName = new File(OUTDATA_PATH);
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                String size = list.size() + "";
                out.write(size);
                out.write("\r\n");
                for (String str : list ) {
                    out.write(str);
                    out.write("\r\n"); // \r\n即为换行
                    out.flush(); // 把缓存区内容压入文件
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
