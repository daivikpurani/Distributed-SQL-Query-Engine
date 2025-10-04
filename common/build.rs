fn main() {
    tonic_build::configure()
        .build_server(true)
        .build_client(true)
        .compile(&["../proto/query.proto"], &["../proto/"])
        .unwrap();
}
