# Java Keystore Generator
Easily generate a java keystore with optional parameters

#### Why
The `keytool` utility shipped with JDKs does not allow the creation or update of a keystore without a password.
This is a simple implementation to work around that

#### Usage
```
usage: KeystoreGenerator [-h] [-o OUTPUT] [-p PASSWORD] [-d PROVIDER] [-f FORMAT] [certs [certs ...]]

Create a BouncyCastle keystore from a set of local certificates

positional arguments:
  certs                  Certificates to add

named arguments:
  -h, --help             show this help message and exit
  -o OUTPUT, --output OUTPUT
                         TrustStore file name (default: TrustStore)
  -p PASSWORD, --password PASSWORD
                         Keystore password (default: )
  -d PROVIDER, --provider PROVIDER
                         Fully qualified Security provider name (default: org.bouncycastle.jce.provider.BouncyCastleProvider)
  -f FORMAT, --format FORMAT
                         Keystore format (default: BKS)
```

#### License
MIT
