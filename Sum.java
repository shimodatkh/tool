import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

public class Sum {
	public static void main(String[] args) {

		String filePath = null;
		String summary_filePath = null;

		// filePath = args[0];
		// String directory = "C:/isucon/";
		String directory = "/home/isucon/log/";
		String renamed_filePath = directory + "access_log2.csv";

		// 引数nullチェック
		int length = args.length;
		if (length == 0) {
			System.out.println("引数を指定して下さい。");
			return;
		}

		// 引数によって変数の値を変更する
		if (args[0].equals("access")) {
			filePath = directory + "access_log.csv";
			summary_filePath = directory + "summary.log";
		} else if (args[0].equals("method")) {
			filePath = directory + "method_prof.log";
			summary_filePath = directory + "summaryMethod.log";
		} else if (args[0].equals("windows")) { // for windows debug
			filePath = "C:/Users/kavo140330/Dropbox/git/log_parse_script/access_log_for_debug.csv";
			summary_filePath = "C:/Users/kavo140330/Dropbox/git/log_parse_script/summary_for_debug.log";
		} else {
			System.out.println("引数はaccessかmethodを指定して下さい。");
			return;
		}

		System.out.println("\n ### Log summarize start... [target log file:"
				+ filePath + "] ###\n");

		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			File summary_file = new File(summary_filePath);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(summary_file, true)));

			Date date = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			renamed_filePath = filePath + "." + sdf1.format(date);

			String line;
			int uri_num = 100;
			String[] registered_request_uris = new String[uri_num];
			double[] time_registered_request_uris = new double[uri_num];
			int[] count_registered_request_uris = new int[uri_num];
			for (double time : time_registered_request_uris) {
				time = 0.0;
			}
			for (int count : count_registered_request_uris) {
				count = 0;
			}

			int num_registered_request_uris = 0;
			boolean is_already_registered = false;

			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				is_already_registered = false;
				String[] log_items = line.split("\t");
				// log_items[0]:date
				// log_items[1]:req time
				// log_items[2]:HTTP Status
				// log_items[3]:Bytes
				// log_items[4]:Method type
				// log_items[5]:req URI

				// URIが登録済みかチェック
				for (int i = 0; i < registered_request_uris.length; i++) {
					if (registered_request_uris[i] != null) {
						String registered_request_uri = registered_request_uris[i];

						// URIが登録済みならリクエストタイムを加算する
						if (registered_request_uri.equals(log_items[5])) {
							time_registered_request_uris[i] += Double
									.parseDouble(log_items[1]);
							is_already_registered = true;
							count_registered_request_uris[i]++;
							break;
						}
					} else {
						break;
					}
				}

				// 初めて出現したURIのみ、登録処理をする
				if (!is_already_registered) {
					registered_request_uris[num_registered_request_uris] = log_items[5];
					time_registered_request_uris[num_registered_request_uris] += Double
							.parseDouble(log_items[1]);
					count_registered_request_uris[num_registered_request_uris]++;
					num_registered_request_uris++;
				}

			}

			// 合計時間算出（割合表示のため）
			double total_time = 0.0;
			for (double d : time_registered_request_uris) {
				total_time += d;
			}
			// System.out.printf("%8.3f\n",total_time);

			// 出力開始
			pw.println("\n### " + sdf1.format(date) + " ###\n");

			TreeSet<DTO> list = new TreeSet<>();

			if (args[0].equals("access")) {
				System.out
						.printf(" Req time[s]     Rate[%%] Count    Ave time[ms]  URI\n");
				pw.printf(" Req time[s]     Rate[%%] Count    AveReq time[s]  URI\n");
			} else {
				System.out
						.printf(" Req time[ms]    Rate[%%] Count    Ave time[us]  URI\n");
				pw.printf(" Req time[ms]    Rate[%%] Count    AveReq time[us] URI\n");
			}

			for (int i = 0; i < registered_request_uris.length; i++) {
				if (registered_request_uris[i] != null) {
					String registered_request_uri = registered_request_uris[i];
					Double time_registered_request_uri = time_registered_request_uris[i];
					DTO dto = new DTO();
					dto.setUri(registered_request_uris[i]);
					dto.setTotalTime(time_registered_request_uris[i]);
					dto.setRate(time_registered_request_uri / total_time
							* 100.0);
					dto.setCount(count_registered_request_uris[i]);
					dto.setAverageTime(time_registered_request_uri
							/ (double) count_registered_request_uris[i]
							* 1000.0);
					list.add(dto);
				} else {
					break;
				}
			}

			for (DTO dto : list) {

				System.out.printf("   %13.3f    %4.1f %8d %13.3f %s \n",
						dto.getTotalTime(), dto.getRate(), dto.getCount(),
						dto.getAverageTime(), dto.getUri());
				pw.printf("   %13.3f    %4.1f %8d %13.3f %s \n",
						dto.getTotalTime(), dto.getRate(), dto.getCount(),
						dto.getAverageTime(), dto.getUri());

			}

			pw.close();

			// 後処理 ログをローテートする

			// 変更前ファイル名
			File fileA = new File(filePath);

			// 変更後のファイル名
			File fileB = new File(renamed_filePath);

			if (fileA.renameTo(fileB)) {
				// ファイル名変更成功
				System.out.println(" ### ファイル名変更成功 ### ");
				System.out.println("\n ### ログローテート完了 [log file:"
						+ renamed_filePath + "] ###\n");

				File newfile = new File(filePath);
				newfile.createNewFile();

				System.out.println(" ### 新しい空ログファイルを作成 ### ");

			} else {
				// ファイル名変更失敗
				System.out.println(" ### ファイル名変更失敗 ### ");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("\n ### Log summarize end...  ###\n");

	}
}

class DTO implements Comparable<DTO> {

	private String uri = "";
	private double totalTime = 0.0;
	private double rate = 0.0;
	private int count = 0;
	private double averageTime = 0.0;

	public double getAverageTime() {
		return averageTime;
	}

	public void setAverageTime(double averageTime) {
		this.averageTime = averageTime;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int compareTo(DTO o) {

		if (this.getTotalTime() == o.getTotalTime()) {
			return 0;
		}

		return this.getTotalTime() < o.getTotalTime() ? 1 : -1;
	}
}
