/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.spec.schemas;

import static com.google.common.base.Preconditions.checkArgument;
import static tech.pegasys.teku.spec.schemas.registry.SchemaTypes.BLS_TO_EXECUTION_CHANGE_SCHEMA;
import static tech.pegasys.teku.spec.schemas.registry.SchemaTypes.HISTORICAL_SUMMARY_SCHEMA;
import static tech.pegasys.teku.spec.schemas.registry.SchemaTypes.SIGNED_BLS_TO_EXECUTION_CHANGE_SCHEMA;
import static tech.pegasys.teku.spec.schemas.registry.SchemaTypes.WITHDRAWAL_SCHEMA;

import java.util.Optional;
import tech.pegasys.teku.spec.config.SpecConfigCapella;
import tech.pegasys.teku.spec.datastructures.blocks.BeaconBlockSchema;
import tech.pegasys.teku.spec.datastructures.blocks.BlockContainer;
import tech.pegasys.teku.spec.datastructures.blocks.BlockContainerSchema;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlockSchema;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBlockContainer;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBlockContainerSchema;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.BeaconBlockBodyBuilder;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.BeaconBlockBodySchema;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.capella.BeaconBlockBodyBuilderCapella;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.capella.BeaconBlockBodySchemaCapellaImpl;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.capella.BlindedBeaconBlockBodySchemaCapellaImpl;
import tech.pegasys.teku.spec.datastructures.builder.BuilderBidSchema;
import tech.pegasys.teku.spec.datastructures.builder.SignedBuilderBidSchema;
import tech.pegasys.teku.spec.datastructures.builder.versions.bellatrix.BuilderBidSchemaBellatrix;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayloadHeaderSchema;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayloadSchema;
import tech.pegasys.teku.spec.datastructures.execution.versions.capella.ExecutionPayloadHeaderSchemaCapella;
import tech.pegasys.teku.spec.datastructures.execution.versions.capella.ExecutionPayloadSchemaCapella;
import tech.pegasys.teku.spec.datastructures.execution.versions.capella.WithdrawalSchema;
import tech.pegasys.teku.spec.datastructures.operations.BlsToExecutionChangeSchema;
import tech.pegasys.teku.spec.datastructures.operations.SignedBlsToExecutionChangeSchema;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.BeaconStateSchema;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.versions.capella.BeaconStateCapella;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.versions.capella.BeaconStateSchemaCapella;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.versions.capella.MutableBeaconStateCapella;
import tech.pegasys.teku.spec.datastructures.state.versions.capella.HistoricalSummary;
import tech.pegasys.teku.spec.schemas.registry.SchemaRegistry;

public class SchemaDefinitionsCapella extends SchemaDefinitionsBellatrix {

  private final BeaconStateSchemaCapella beaconStateSchema;

  private final ExecutionPayloadSchemaCapella executionPayloadSchemaCapella;
  private final ExecutionPayloadHeaderSchemaCapella executionPayloadHeaderSchemaCapella;

  private final BeaconBlockBodySchemaCapellaImpl beaconBlockBodySchema;
  private final BlindedBeaconBlockBodySchemaCapellaImpl blindedBeaconBlockBodySchema;

  private final BeaconBlockSchema beaconBlockSchema;
  private final BeaconBlockSchema blindedBeaconBlockSchema;
  private final SignedBeaconBlockSchema signedBeaconBlockSchema;
  private final SignedBeaconBlockSchema signedBlindedBeaconBlockSchema;

  private final WithdrawalSchema withdrawalSchema;

  private final BlsToExecutionChangeSchema blsToExecutionChangeSchema;

  private final SignedBlsToExecutionChangeSchema signedBlsToExecutionChangeSchema;
  private final BuilderBidSchema<?> builderBidSchemaCapella;
  private final SignedBuilderBidSchema signedBuilderBidSchemaCapella;

  private final HistoricalSummary.HistoricalSummarySchema historicalSummarySchema;

  public SchemaDefinitionsCapella(final SchemaRegistry schemaRegistry) {
    super(schemaRegistry);
    final SpecConfigCapella specConfig = SpecConfigCapella.required(schemaRegistry.getSpecConfig());
    this.historicalSummarySchema = schemaRegistry.get(HISTORICAL_SUMMARY_SCHEMA);
    this.executionPayloadSchemaCapella = new ExecutionPayloadSchemaCapella(specConfig);
    this.blsToExecutionChangeSchema = schemaRegistry.get(BLS_TO_EXECUTION_CHANGE_SCHEMA);
    this.signedBlsToExecutionChangeSchema =
        schemaRegistry.get(SIGNED_BLS_TO_EXECUTION_CHANGE_SCHEMA);
    this.withdrawalSchema = schemaRegistry.get(WITHDRAWAL_SCHEMA);

    this.beaconStateSchema = BeaconStateSchemaCapella.create(specConfig);
    this.executionPayloadHeaderSchemaCapella =
        beaconStateSchema.getLastExecutionPayloadHeaderSchema();
    this.beaconBlockBodySchema =
        BeaconBlockBodySchemaCapellaImpl.create(
            specConfig,
            signedBlsToExecutionChangeSchema,
            getMaxValidatorsPerAttestation(specConfig),
            "BeaconBlockBodyCapella",
            schemaRegistry);
    this.blindedBeaconBlockBodySchema =
        BlindedBeaconBlockBodySchemaCapellaImpl.create(
            specConfig,
            signedBlsToExecutionChangeSchema,
            getMaxValidatorsPerAttestation(specConfig),
            "BlindedBlockBodyCapella",
            schemaRegistry);
    this.beaconBlockSchema = new BeaconBlockSchema(beaconBlockBodySchema, "BeaconBlockCapella");
    this.blindedBeaconBlockSchema =
        new BeaconBlockSchema(blindedBeaconBlockBodySchema, "BlindedBlockCapella");
    this.signedBeaconBlockSchema =
        new SignedBeaconBlockSchema(beaconBlockSchema, "SignedBeaconBlockCapella");
    this.signedBlindedBeaconBlockSchema =
        new SignedBeaconBlockSchema(blindedBeaconBlockSchema, "SignedBlindedBlockCapella");
    this.builderBidSchemaCapella =
        new BuilderBidSchemaBellatrix("BuilderBidCapella", executionPayloadHeaderSchemaCapella);
    this.signedBuilderBidSchemaCapella =
        new SignedBuilderBidSchema("SignedBuilderBidCapella", builderBidSchemaCapella);
  }

  public static SchemaDefinitionsCapella required(final SchemaDefinitions schemaDefinitions) {
    checkArgument(
        schemaDefinitions instanceof SchemaDefinitionsCapella,
        "Expected definitions of type %s but got %s",
        SchemaDefinitionsCapella.class,
        schemaDefinitions.getClass());
    return (SchemaDefinitionsCapella) schemaDefinitions;
  }

  @Override
  public BeaconStateSchema<? extends BeaconStateCapella, ? extends MutableBeaconStateCapella>
      getBeaconStateSchema() {
    return beaconStateSchema;
  }

  @Override
  public BeaconBlockBodySchema<?> getBeaconBlockBodySchema() {
    return beaconBlockBodySchema;
  }

  @Override
  public BeaconBlockBodySchema<?> getBlindedBeaconBlockBodySchema() {
    return blindedBeaconBlockBodySchema;
  }

  @Override
  public BeaconBlockSchema getBeaconBlockSchema() {
    return beaconBlockSchema;
  }

  @Override
  public BeaconBlockSchema getBlindedBeaconBlockSchema() {
    return blindedBeaconBlockSchema;
  }

  @Override
  public SignedBeaconBlockSchema getSignedBeaconBlockSchema() {
    return signedBeaconBlockSchema;
  }

  @Override
  public SignedBeaconBlockSchema getSignedBlindedBeaconBlockSchema() {
    return signedBlindedBeaconBlockSchema;
  }

  @Override
  public BlockContainerSchema<BlockContainer> getBlockContainerSchema() {
    return getBeaconBlockSchema().castTypeToBlockContainer();
  }

  @Override
  public BlockContainerSchema<BlockContainer> getBlindedBlockContainerSchema() {
    return getBlindedBeaconBlockSchema().castTypeToBlockContainer();
  }

  @Override
  public SignedBlockContainerSchema<SignedBlockContainer> getSignedBlockContainerSchema() {
    return getSignedBeaconBlockSchema().castTypeToSignedBlockContainer();
  }

  @Override
  public SignedBlockContainerSchema<SignedBlockContainer> getSignedBlindedBlockContainerSchema() {
    return getSignedBlindedBeaconBlockSchema().castTypeToSignedBlockContainer();
  }

  @Override
  public ExecutionPayloadSchema<?> getExecutionPayloadSchema() {
    return executionPayloadSchemaCapella;
  }

  @Override
  public ExecutionPayloadHeaderSchema<?> getExecutionPayloadHeaderSchema() {
    return executionPayloadHeaderSchemaCapella;
  }

  @Override
  public BeaconBlockBodyBuilder createBeaconBlockBodyBuilder() {
    return new BeaconBlockBodyBuilderCapella(beaconBlockBodySchema, blindedBeaconBlockBodySchema);
  }

  public WithdrawalSchema getWithdrawalSchema() {
    return withdrawalSchema;
  }

  public BlsToExecutionChangeSchema getBlsToExecutionChangeSchema() {
    return blsToExecutionChangeSchema;
  }

  public SignedBlsToExecutionChangeSchema getSignedBlsToExecutionChangeSchema() {
    return signedBlsToExecutionChangeSchema;
  }

  public HistoricalSummary.HistoricalSummarySchema getHistoricalSummarySchema() {
    return historicalSummarySchema;
  }

  @Override
  public BuilderBidSchema<?> getBuilderBidSchema() {
    return builderBidSchemaCapella;
  }

  @Override
  public SignedBuilderBidSchema getSignedBuilderBidSchema() {
    return signedBuilderBidSchemaCapella;
  }

  @Override
  public Optional<SchemaDefinitionsCapella> toVersionCapella() {
    return Optional.of(this);
  }
}
