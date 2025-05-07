package com.master.mosaique_capital;

import org.springframework.boot.SpringApplication;

public class TestMosaiqueCapitalApplication {

    public static void main(String[] args) {
        SpringApplication.from(MosaiqueCapitalApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
