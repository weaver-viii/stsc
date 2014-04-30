package stsc.simulator.multistarter;

import stsc.common.FromToPeriod;

public class AlgorithmSettingsIteratorFactory {

	private final FromToPeriod period;
	private boolean finished;

	private final ParameterList[] parameters = { new ParameterList(ParameterType.integerType),
			new ParameterList(ParameterType.doubleType), new ParameterList(ParameterType.stringType),
			new ParameterList(ParameterType.subExecutionType) };

	public AlgorithmSettingsIteratorFactory(final FromToPeriod period) {
		this.period = period;
		this.finished = false;
	}

	public void add(final MpInteger parameter) {
		if (parameter.hasNext())
			this.finished = false;
		parameters[ParameterType.integerType.getValue()].add(parameter);
	}

	public void add(final MpDouble parameter) {
		if (parameter.hasNext())
			this.finished = false;
		parameters[ParameterType.doubleType.getValue()].add(parameter);
	}

	public void add(final MpString parameter) {
		if (parameter.hasNext())
			this.finished = false;
		parameters[ParameterType.stringType.getValue()].add(parameter);
	}

	public void add(final MpSubExecution parameter) {
		if (parameter.hasNext())
			this.finished = false;
		parameters[ParameterType.subExecutionType.getValue()].add(parameter);
	}

	public AlgorithmSettingsGridIterator getGridIterator() {
		return new AlgorithmSettingsGridIterator(period, finished, parameters);
	}
}
