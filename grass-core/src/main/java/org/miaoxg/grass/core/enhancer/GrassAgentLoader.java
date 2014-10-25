package org.miaoxg.grass.core.enhancer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.security.CodeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

/**
 * 入口类，在应用系统环境初始化之前调用loadAgent加载GrassAgent
 * 
 * agent方式是加载jar包，然后根据meta-inf中的agent-class寻找GrassAgent类，而不是指定加载的具体类
 */
public class GrassAgentLoader {

	private final Logger logger = LoggerFactory.getLogger(GrassAgentLoader.class);
	
	private static final GrassAgentLoader loader = new GrassAgentLoader();
	
	private GrassAgentLoader() {
	}
	
	public static GrassAgentLoader instance() {
		return loader;
	}

    public void loadAgent() {
        logger.info("dynamically loading javaagent");
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);  // 进程id
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            CodeSource codeSource = GrassAgent.class.getProtectionDomain().getCodeSource();
            
            // windows下, 路径以“/‘开头，需去掉
            String jarPath = new File(codeSource.getLocation().toURI().getPath()).getAbsolutePath();
            vm.loadAgent(jarPath, "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("load agent done");
    }
}
