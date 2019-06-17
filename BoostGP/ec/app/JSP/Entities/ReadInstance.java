package ec.app.JSP.Entities;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ReadInstance {

    static void readUntil(Scanner in, String str)
    {
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();

//            System.out.println(line);

            if (line.startsWith(str)) {

                break;
            }
        }
    }
    
    public static void main(String[] args){

    	  Scanner in = null;
          try {
        	  System.out.println(new File("1.txt").getAbsolutePath());
              in = new Scanner(new File("1.txt"));
              JobShop problem = ReadInstance.readOne(in);
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }

       

  }

     static void readUntil(Scanner in) {
        while (true) {
            String line = in.nextLine().trim();

            if (line.startsWith("+++")) {

                break;
            }
        }
    }

     public  static JobShop readOne(Scanner in) {

        System.out.println(in.nextLine());
        String temp[] = in.nextLine().trim().split("\\s+");

        System.out.println(Arrays.toString(temp));

        int numJob = Integer.parseInt(temp[0]);

         int numM = Integer.parseInt(temp[1]);



        List<List<Integer>> opM = new ArrayList<>();

        List<List<Double>> opP = new ArrayList<>();

        List<Double> weights = new ArrayList<>();

        List<Double> dues = new ArrayList<>();


        for (int i = 0; i < numJob; i++) {

            weights.add(1.0);

            dues.add(10000.0);

            List<Integer> l = new ArrayList<>();

            List<Double> p = new ArrayList<>();

            String line = in.nextLine().trim();

            String t[] = line.split("\\s+");

//            System.out.println(Arrays.toString(t));

            for (int k = 0; k < t.length; k += 2) {

                l.add(Integer.parseInt(t[k]));

                p.add(Double.parseDouble(t[k + 1]));
            }


            opM.add(l);

            opP.add(p);


        }

        System.out.println(numM);
        System.out.println(opM);

        System.out.println(opP);

        JobShop jsp = new JobShop();
        jsp.Initialize(numM, opM, opP, weights, dues);
        return jsp;
    }
}
