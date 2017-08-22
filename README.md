# DBポーリングのステータス変更に対応したシンプルなバッチ起動サンプル

## ビルド・起動方法

* ビルド(Maven + Java8が必要)

```bash
$ cd WORK_DIR/daemon-with-state
$ mvn clean package
```

* 起動(停止方法は後述)
```bash
$ cd target
$ java -jar daemon-with-state-0.0.1-SNAPSHOT.jar
```

## 起動時に一定間隔でDBのポーリングを行い、ジョブを起動する。
* 一定間隔(10秒)に一回ジョブの刈り取りを行う。
* DBはH2を使用する。ジョブ刈り取りのテーブルは`src/resources/schema-h2.sql`を起動時に生成する。

## ステータス一覧()内はメッセージ
* POLLING(polling) ：　一定間隔で実行対象のジョブを刈り取る。  
ポーリング(polling)メッセージを送ると即時ジョブ刈り取り・実行する。  
* シャットダウン(shutdown) ： アプリケーションを終了する。  
遷移先はなし。
* 中断(pause) ： ポーリングを中断する。  
ポーリング再開(restart)メッセージを送ることでPOLLING状態に戻る。
* 遷移可能なステータスは下図参照

![statusflow image](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuOhMYbNGrRLJ2F3tyV7qS-U2qX0nnz1WLmKhXOB4qk9KBWWFIIrGfYWLR92EGa5-JavcNZggThWok2pSY62Fq10N_t0_e6HnHcb9Idvv7efURF9mCP02eBkv75BpKa1-0000 "有効な状態遷移")

## メッセージの送信
* UDP（以降、アプリケーション起動ホスト localhost ポート 12345 とする。）で、起動中のアプリケーションホストに"mode=XXXX"の形式で送信する。
* 下記例ではLinux/Mac等で利用可能なnetcat(nc)コマンドを用いる。※Windowsではウィルスの誤検出の前例があるため、netcatをインストールしないこと！！
* ポーリング
```bash
$ echo -en "mode=polling" | nc -u localhost 12345
```

* 中断
```bash
$ echo -en "mode=pause" | nc -u localhost 12345
```

* 再開
```bash
$ echo -en "mode=restart" | nc -u localhost 12345
```

* シャットダウン
```bash
$ echo -en "mode=shutdown" | nc -u localhost 12345
```

* echo は改行なし(-en)で送信する。

## ジョブの登録
* H2コンソール `http://apphost:8080/console/` で以下項目を設定し、Connect
  - JDBC URL: jdbc:h2:~/.h2/batch5-test
  - User Name: sa
  - Password: (空欄)

* `BATCH_JOB_REQUEST`テーブルに対し、以下の要領でINSERTを実行する。
（カスタムなジョブをこのアプリケーションに追加していない場合はジョブ名は`sampleJob`固定）

```sql
INSERT INTO BATCH_JOB_REQUEST (JOB_NAME, POLLING_STATUS, CREATE_DATE) VALUES ('sampleJob', 'INIT', CURRENT_TIMESTAMP);
```

## TIPS
* 即時ポーリング時、実行対象のジョブ名、パラメータを指定可能
(実際に対応するジョブを作っておかないと実行できない。)

```
$ echo -en "mode=polling,job=customJob,param1=value1,param2=valur2" | nc -u localhost 12345
```

## 設計について
* GoFデザインパターンのStateパターンを使用しており、コントローラ部のswitch-caseによる煩雑な状態管理を行わないようにしている。
* （ThreadPoolTask)Executorは１つ。ジョブ実行以外のスレッドはメインスレッドとメッセージ受信スレッドの2スレッドのみ。
* 1スレッドで即時ポーリングを実現できるよう、UDP受信のSO_TIMEOUTをポーリング間隔として併用している。
