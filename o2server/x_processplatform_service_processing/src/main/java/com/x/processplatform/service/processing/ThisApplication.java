package com.x.processplatform.service.processing;

import java.util.concurrent.ForkJoinPool;

import org.apache.commons.lang3.BooleanUtils;

import com.x.base.core.project.ApplicationForkJoinWorkerThreadFactory;
import com.x.base.core.project.Context;
import com.x.base.core.project.cache.CacheManager;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.message.MessageConnector;
import com.x.processplatform.service.processing.processor.invoke.SyncJaxrsInvokeQueue;
import com.x.processplatform.service.processing.processor.invoke.SyncJaxwsInvokeQueue;
import com.x.processplatform.service.processing.schedule.ArchiveHadoop;
import com.x.processplatform.service.processing.schedule.CleanEvent;
import com.x.processplatform.service.processing.schedule.DeleteDraft;
import com.x.processplatform.service.processing.schedule.Expire;
import com.x.processplatform.service.processing.schedule.LogLongDetained;
import com.x.processplatform.service.processing.schedule.Merge;
import com.x.processplatform.service.processing.schedule.PassExpired;
import com.x.processplatform.service.processing.schedule.TouchDelay;
import com.x.processplatform.service.processing.schedule.TouchDetained;
import com.x.processplatform.service.processing.schedule.UpdateTable;
import com.x.processplatform.service.processing.schedule.Urge;

public class ThisApplication {

	private ThisApplication() {
		// nothing
	}

	private static final ForkJoinPool FORKJOINPOOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
			new ApplicationForkJoinWorkerThreadFactory(ThisApplication.class.getPackage()), null, false);

	public static ForkJoinPool forkJoinPool() {
		return FORKJOINPOOL;
	}

	protected static Context context;

	public static final SyncJaxrsInvokeQueue syncJaxrsInvokeQueue = new SyncJaxrsInvokeQueue();

	public static final SyncJaxwsInvokeQueue syncJaxwsInvokeQueue = new SyncJaxwsInvokeQueue();

	public static final UpdateTableQueue updateTableQueue = new UpdateTableQueue();

	public static final ArchiveHadoopQueue archiveHadoopQueue = new ArchiveHadoopQueue();

	private static ProcessingToProcessingSignalStack processingToProcessingSignalStack = new ProcessingToProcessingSignalStack();

	public static ProcessingToProcessingSignalStack getProcessingToProcessingSignalStack() {
		return processingToProcessingSignalStack;
	}

	public static Context context() {
		return context;
	}

	public static void init() {
		try {
			CacheManager.init(context.clazz().getSimpleName());
			MessageConnector.start(context());
			context().startQueue(syncJaxrsInvokeQueue);
			context().startQueue(syncJaxwsInvokeQueue);
			context().startQueue(updateTableQueue);
			context().startQueue(archiveHadoopQueue);
			if (BooleanUtils.isTrue(Config.processPlatform().getMerge().getEnable())) {
				context.schedule(Merge.class, Config.processPlatform().getMerge().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getDeleteDraft().getEnable())) {
				context.schedule(DeleteDraft.class, Config.processPlatform().getDeleteDraft().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getExpire().getEnable())) {
				context.schedule(Expire.class, Config.processPlatform().getExpire().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getLogLongDetained().getEnable())) {
				context.schedule(LogLongDetained.class, Config.processPlatform().getLogLongDetained().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getPassExpired().getEnable())) {
				context.schedule(PassExpired.class, Config.processPlatform().getPassExpired().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getTouchDelay().getEnable())) {
				context.schedule(TouchDelay.class, Config.processPlatform().getTouchDelay().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getTouchDetained().getEnable())) {
				context.schedule(TouchDetained.class, Config.processPlatform().getTouchDetained().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getUrge().getEnable())) {
				context.schedule(Urge.class, Config.processPlatform().getUrge().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getUpdateTable().getEnable())) {
				context.schedule(UpdateTable.class, Config.processPlatform().getUpdateTable().getCron());
			}
			if (BooleanUtils.isTrue(Config.processPlatform().getArchiveHadoop().getEnable())) {
				context.schedule(ArchiveHadoop.class, Config.processPlatform().getArchiveHadoop().getCron());
			}
			context.schedule(CleanEvent.class, "40 40 * * * ?");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void destroy() {
		try {
			FORKJOINPOOL.shutdown();
			CacheManager.shutdown();
			MessageConnector.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
