# Encrypted Distributed File Storage

The objective is to store sensitive information in an encrypted manner. The encrypted content is stored in a distributed manner in a multi cloud environment to have an additional layer of security.

The files are encrypted and decrypted using Chinese Remainder Theorem (CRT). The user needs to use a private key to upload the content, the same key needs to be used inorder to decrypt the contents.

## Theory
### Chinese Remainder Theorem

#### Theorem
<pre>
Let p, q be coprime.
Then the system of equations
x ≡ a<sub>i</sub> (mod m<sub>i</sub>)   for 1 ≤ i ≤ k
has a unique solution for
x modulo m<sub>1</sub>.m<sub>2</sub> … m<sub>k</sub>
</pre>

#### Encryption
The encryption is relatively simpler and computationally lighter in case of Chinese Remainder Theorem in comparison to the decryption process.

The algorithm required N coprime key values, for the scope of this report we will consider the use of 3 cloud endpoints. Thus, we need 3 key values which are coprime.

The content is encrypted with simple modulo operation using the keys over the 3 key values.
<pre>
Say ‘x’ is the original content, k<sub>1</sub> = key<sub>1</sub>, k<sub>2</sub> = key<sub>2</sub>, k<sub>3</sub> = key<sub>3</sub>

Enc content 1: r<sub>1</sub> ≡ x mod k<sub>1</sub>
Enc content 2: r<sub>2</sub> ≡ x mod k<sub>2</sub>
Enc content 3: r<sub>3</sub> ≡ x mod k<sub>3</sub>
</pre>

#### File Upload
In order to encrypt the full file, the file is read as a stream of bytes. Each byte is considered as X and encoded with the above operation for the corresponding account and key value.

This ensures that each of the encrypted files has a distinct set of values ( r<sub>i</sub> ) and none of them individually are sufficient to decrypt the content.

#### Decryption
As mentioned earlier, decryption is comparatively computationally more expensive than encryption.

<pre>
x =  ( ∑ ( r<sub>i</sub> * ( product / k<sub>i</sub> ) * inv<sub>i</sub> ) ) % product

Where 1 ≤ i ≤ n
‘n’ being the number of keys or cloud accounts
‘r<sub>i</sub>’ is encrypted content distributed across cloud accounts
‘product’ = k<sub>1</sub> * k<sub>2</sub> … * k<sub>n</sub>
‘inv<sub>i</sub>’ = Modular Multiplicative Inverse of ( product / k<sub>i</sub> )
</pre>

#### Modular Multiplicative Inverse
Modular multiplicative inverse of an integer N is equal to the integer which when multiplied to the number N is congruent to 1 with respect to the modulo M

```math
i.e.   N . x ≡ 1 mod M
```

Computing modular inverse is a heavy operation as this is more of an iterative process, more like a hit and trial approach.
The naive approach would be to traverse and substitute x with all values from 0 to M inclusive. Time Complexity: O(N).
However, when we have the guarantees for N and M to be coprime in nature, we can use the Extended Euclidean Algorithm for more efficient computation.

##### Extended Euclidean Algorithm
The GCD of 2 numbers do not change if the smaller number is subtracted from the larger number.

```math
In this case,
N, M are coprime

=> gcd(N,M) = 1
=> x.N + y.M = 1
=> x.N - 1 = (- y).M
=> N . x ≡ 1 mod M

Thus, x is the modular inverse of N wrt M.

Using the following downsizing approach, we can find the inverse in logarithmic time
x’ = y - ⌊N/M⌋ * x
y’ = x
```

Time Complexity: O(log N)

#### File Download
The similar decryption process is done for each byte in the stream of bytes from multiple files in the different cloud account.
The decrypted values are written as a different file which will be identical to the original file which was uploaded.


## Setup
Minio Docker Setup
```
docker run -d --name minio -p 9000:9000 -e "MINIO_ACCESS_KEY=accessKey" -e "MINIO_SECRET_KEY=secretKey" minio/minio server /home/pratik/personal/dev/minio
```

Create buckets:
my-bucket-0, my-bucket-1, my-bucket-2


Build fat jar
```
cd encrypted-distributed-file-storage
mvn clean install -Pfat-jars
```

Upload Encrypted File
```
java -Dfile=src/main/resources/text.txt -Dout=ENC_FILE -Dkey=src/main/resources/key.pem -cp target/encrypted-distributed-file-storage-1.0-SNAPSHOT-jar-with-dependencies.jar filesystem.FileUploader
```

Download Decrypted File
```
java -Dfile=ENC_FILE -Dout=/tmp/Downloads -Dkey=src/main/resources/key.pem -cp target/encrypted-distributed-file-storage-1.0-SNAPSHOT-jar-with-dependencies.jar filesystem.FileDownloader
```
